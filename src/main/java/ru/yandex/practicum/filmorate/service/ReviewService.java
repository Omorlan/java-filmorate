package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorageDb;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewService {
    private final ReviewStorageDb reviewStorageDb;

    public Review create(Review review) {
        return reviewStorageDb.create(review);
    }

    public Review update(Review review) {
        return reviewStorageDb.update(review);
    }

    public void remove(Long id) {
        reviewStorageDb.remove(id);
    }

    public List<Review> findAll() {
        return reviewStorageDb.findAll();
    }

    public Review getReview(Long id) {
        return reviewStorageDb.getReview(id).orElseThrow(
                () -> new NotFoundException("Отзыв с id = " + id + " не найден"));
    }

    public List<Review> getReviewsByFilm(Long filmId, int count) {
        return reviewStorageDb.getReviewsByFilm(filmId, count);
    }

    public void addLike(Long reviewId, Long userId) {
        reviewStorageDb.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        reviewStorageDb.addDislike(reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        reviewStorageDb.removeReactionFromReview(reviewId, userId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        reviewStorageDb.removeReactionFromReview(reviewId, userId);
    }
}
