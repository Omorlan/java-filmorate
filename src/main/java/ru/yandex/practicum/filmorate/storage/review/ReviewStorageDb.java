package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReviewStorageDb implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review create(Review review) {
        checkUserAndFilmExistence(review.getUserId(), review.getFilmId());
        review.setUseful(0L);
        log.info("Creating review: {}", review);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        final String sqlQuery = """
                INSERT INTO reviews (content, is_positive, user_id, film_id, useful)
                VALUES (?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"review_id"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setLong(3, review.getUserId());
            stmt.setLong(4, review.getFilmId());
            stmt.setLong(5, review.getUseful());
            return stmt;
        }, keyHolder);

        long reviewId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        review.setReviewId(reviewId);
        log.info("Review created: {}", review);
        return review;
    }

    @Override
    public Review update(Review review) {
        checkUserAndFilmExistence(review.getUserId(), review.getFilmId());
        log.info("Updating review: {}", review);
        Review reviewToUpdate = getReview(review.getReviewId()).orElseThrow(() ->
                new NotFoundException("Review with id = " + review.getReviewId() + " not found"));
        final String sqlQuery = """
                UPDATE reviews
                SET content = ?, is_positive = ?, user_id = ?, film_id = ?, useful = ?
                WHERE review_id = ?
                """;
        jdbcTemplate.update(
                sqlQuery,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getUseful(),
                review.getReviewId()
        );
        Review updatedReview = getReview(review.getReviewId())
                .orElseThrow(() -> new NotFoundException("Review id = " + review.getReviewId() + " not found"));

        log.info("Review updated: {}", updatedReview);
        return updatedReview;
    }

    @Override
    public void remove(Long id) {
        log.info("Removing review with id: {}", id);
        Review reviewToRemove = getReview(id).orElseThrow(() ->
                new NotFoundException("Review with id = " + id + " not found"));
        final String sqlQuery = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sqlQuery, id);
        log.info("Review with id {} removed", id);
    }

    @Override
    public List<Review> findAll() {
        log.info("Fetching all reviews");
        return jdbcTemplate.query("SELECT * FROM reviews", new ReviewMapper());
    }

    @Override
    public Optional<Review> getReview(Long reviewId) {
        String sqlQuery = """
                SELECT review_id, content, is_positive, user_id, film_id, useful
                FROM reviews
                WHERE review_id = ?
                """;

        return jdbcTemplate.query(sqlQuery, new ReviewMapper(), reviewId).stream().findFirst();
    }

    @Override
    public List<Review> getReviewsByFilm(Long filmId, Integer count) {
        log.info("Fetching reviews for film with id: {}", filmId);
        String sqlQuery;
        List<Review> reviews;

        if (count == null || count <= 0) {
            count = 10;
        }

        if (filmId == null) {
            sqlQuery = "SELECT * FROM reviews LIMIT ?";
            reviews = jdbcTemplate.query(sqlQuery, new ReviewMapper(), count);
        } else {
            sqlQuery = "SELECT * FROM reviews WHERE film_id = ? LIMIT ?";
            reviews = jdbcTemplate.query(sqlQuery, new ReviewMapper(), filmId, count);
        }

        log.info("Fetched {} reviews", reviews.size());
        return reviews;
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        addReactionToReview(reviewId, userId, Boolean.TRUE);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        addReactionToReview(reviewId, userId, Boolean.FALSE);
    }

    public void addReactionToReview(Long reviewId, Long userId, boolean isPositive) {
        String sqlQuery = """
                MERGE INTO review_likes (
                review_id, user_id, is_useful
                )
                VALUES (?, ?, ?)
                """;

        jdbcTemplate.update(sqlQuery, reviewId, userId, isPositive);
        updateReviewRating(reviewId);
    }

    public void removeReactionFromReview(Long reviewId, Long userId) {
        String sqlQuery = """
                DELETE FROM review_likes
                WHERE review_id = ? AND user_id = ?
                """;

        jdbcTemplate.update(sqlQuery, reviewId, userId);
        updateReviewRating(reviewId);
    }

    private void updateReviewRating(Long reviewId) {
        long usefulRating = getReviewUseful(reviewId);

        String sqlQuery = """
                UPDATE reviews SET useful = ?
                WHERE review_id = ?
                """;

        jdbcTemplate.update(sqlQuery, usefulRating, reviewId);
    }

    private long getReviewUseful(long reviewId) {
        String sqlQuery = """
                SELECT SUM(
                CASE WHEN is_useful = TRUE THEN 1 ELSE -1 END) useful
                FROM review_likes
                WHERE review_id = ?
                """;

        return jdbcTemplate.query(sqlQuery, new ReviewRatingMapper(), reviewId).stream().findAny().orElseThrow(() ->
                new NotFoundException("Review id = " + reviewId + " not found"));
    }

    private static class ReviewRatingMapper implements RowMapper<Long> {
        @Override
        public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getLong("useful");
        }
    }

    private void checkUserAndFilmExistence(Long userId, Long filmId) {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(filmId, "filmId cannot be null");

        final String userQuery = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        final String filmQuery = "SELECT COUNT(*) FROM films WHERE film_id = ?";

        Integer userCount = jdbcTemplate.queryForObject(userQuery, Integer.class, userId);
        Integer filmCount = jdbcTemplate.queryForObject(filmQuery, Integer.class, filmId);

        if (userCount == null || userCount == 0) {
            throw new NotFoundException("User with id " + userId + " does not exist");
        }
        if (filmCount == null || filmCount == 0) {
            throw new NotFoundException("Film with id " + filmId + " does not exist");
        }
    }

}
