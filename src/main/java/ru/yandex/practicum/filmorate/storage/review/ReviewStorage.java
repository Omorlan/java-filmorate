package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    void remove(Long id);

    List<Review> findAll();

    Optional<Review> getReview(Long reviewId);

    List<Review> getReviewsByFilm(Long filmId, Integer count);

    void addLike(Long reviewId, Long userId);

    void addDislike(Long reviewId, Long userId);
}
