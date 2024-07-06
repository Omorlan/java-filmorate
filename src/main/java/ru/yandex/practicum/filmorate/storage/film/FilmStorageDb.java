package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
@Getter
public class FilmStorageDb implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        log.info("Creating film: {}", film);
        if (film.getMpa().getId() > 5) {
            log.error("Invalid MPA id: {}", film.getMpa().getId());
            throw new ValidationException("Неверно задан MPA");
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        final String sqlQuery = """
                INSERT INTO films (film_name, film_description, film_duration, film_releaseDate, mpa_id)
                VALUES (?,?,?,?,?)
                """;
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setLong(3, film.getDuration());
            stmt.setDate(4, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        long filmId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            final String genreInsertQuery = """
                    INSERT INTO film_genres (film_id, genre_id)
                    VALUES (?, ?)
                    """;
            jdbcTemplate.batchUpdate(genreInsertQuery, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Genre genre = (Genre) film.getGenres().toArray()[i];
                    if (genre.getId() > 6) {
                        log.error("Invalid genre id: {}", genre.getId());
                        throw new ValidationException("Такого жанра с id = " + genre.getId() + " нет");
                    }
                    ps.setLong(1, filmId);
                    ps.setLong(2, genre.getId());
                }

                @Override
                public int getBatchSize() {
                    return film.getGenres().size();
                }
            });
        }
        log.info("Film created: {}", film);
        return film;
    }

    @Override
    public void remove(Long id) {
        log.info("Removing film with id: {}", id);
        String sqlQuery = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, id);
        log.info("Film with id {} removed", id);
    }

    @Override
    public Film update(Film newFilm) {
        log.info("Updating film: {}", newFilm);
        if (newFilm.getId() == null) {
            log.error("Film id is missing during update");
            throw new ValidationException("The ID must be specified");
        }
        final String sqlQuery = """
                UPDATE films
                SET film_name = ?, film_description = ?, film_duration = ?, film_releaseDate = ?, mpa_id = ?
                WHERE film_id = ?
                """;
        jdbcTemplate.update(
                sqlQuery,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getDuration(),
                newFilm.getReleaseDate(),
                newFilm.getMpa().getId(),
                newFilm.getId()
        );
        Film updatedFilm = getFilmById(newFilm.getId());
        log.info("Film updated: {}", updatedFilm);
        return updatedFilm;
    }

    @Override
    public List<Film> findAll() {
        log.info("Fetching all films");
        final String sqlQuery = """
                    SELECT f.*,
                           m.mpa_id, m.mpa_name,
                           g.genre_id, g.genre_name,
                           l.user_id AS like_user_id
                    FROM films f
                    JOIN mpa m ON f.mpa_id = m.mpa_id
                    LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                    LEFT JOIN genres g ON fg.genre_id = g.genre_id
                    LEFT JOIN likes l ON f.film_id = l.film_id
                """;
        List<Film> films = jdbcTemplate.query(sqlQuery, new FilmMapper());
        log.info("Fetched {} films", films.size());
        return films;
    }

    @Override
    public Film getFilmById(Long id) {
        log.info("Fetching film with id: {}", id);
        List<Film> films = findAll();
        Optional<Film> optionalFilm = films.stream()
                .filter(f -> f.getId().equals(id))
                .findFirst();

        if (optionalFilm.isPresent()) {
            log.info("Film found: {}", optionalFilm.get());
            return optionalFilm.get();
        } else {
            log.error("Film with id {} not found", id);
            throw new NotFoundException("Film id = " + id + " not found");
        }
    }

    @Override
    public void addLike(Long id, Long userId) {
        log.info("Adding like to film with id: {} from user with id: {}", id, userId);
        getFilmById(id);
        final String sqlQuery = """
                INSERT INTO likes (film_id, user_id)
                VALUES(?,?)
                """;
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery);
            stmt.setLong(1, id);
            stmt.setLong(2, userId);
            return stmt;
        });
        log.info("Like added to film with id: {} from user with id: {}", id, userId);
    }

    @Override
    public void deleteLike(Long id, Long userId) {
        log.info("Deleting like from film with id: {} by user with id: {}", id, userId);
        final String sqlQuery = """
                DELETE FROM likes
                WHERE film_id = ? AND user_id = ?
                """;
        jdbcTemplate.update(sqlQuery, id, userId);
        log.info("Like deleted from film with id: {} by user with id: {}", id, userId);
    }

    @Override
    public List<Film> getPopularFilms(Long count) {
        log.info("Fetching top {} popular films", count);
        final String sqlQuery = """
                SELECT f.*,
                       m.mpa_id, m.mpa_name,
                       g.genre_id, g.genre_name,
                       l.user_id AS like_user_id
                FROM (
                    SELECT f.film_id, COUNT(l.user_id) as like_count
                    FROM films f
                    LEFT JOIN likes l ON f.film_id = l.film_id
                    GROUP BY f.film_id
                    ORDER BY like_count DESC
                    LIMIT ?
                ) popular
                JOIN films f ON popular.film_id = f.film_id
                JOIN mpa m ON f.mpa_id = m.mpa_id
                LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                LEFT JOIN genres g ON fg.genre_id = g.genre_id
                LEFT JOIN likes l ON f.film_id = l.film_id
                ORDER BY popular.like_count DESC, f.film_id
                """;
        List<Film> popularFilms = jdbcTemplate.query(sqlQuery, new FilmMapper(), count);
        log.info("Fetched {} popular films", popularFilms.size());
        return popularFilms;
    }
}