package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.OperationType;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorageDb;

import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewService {
    private final ReviewStorageDb reviewStorageDb;
    private final FeedService feedService;

    public Review create(Review review) {
        Review result = reviewStorageDb.create(review);
        feedService.createEvent(
                review.getUserId(),
                EventType.REVIEW,
                OperationType.ADD,
                result.getReviewId()
        );
        return result;
    }

    public Review update(Review review) {
        Review result = reviewStorageDb.update(review);
        feedService.createEvent(
                review.getUserId(),
                EventType.REVIEW,
                OperationType.UPDATE,
                result.getReviewId()
        );
        return result;
    }

    public void remove(Long id) {
        Review review = getReview(id);
        feedService.createEvent(
                review.getUserId(),
                EventType.REVIEW,
                OperationType.REMOVE,
                id
        );
        reviewStorageDb.remove(id);
    }

    public List<Review> findAll() {
        return reviewStorageDb.findAll();
    }

    public Review getReview(Long id) {
        return reviewStorageDb.getReview(id).orElseThrow(
                () -> new NotFoundException("Review with id = " + id + " not found"));
    }

    public List<Review> getReviewsByFilm(Long filmId, int count) {
        return reviewStorageDb.getReviewsByFilm(filmId, count).stream()
                .sorted(Comparator.comparingLong(Review::getUseful).reversed())
                .toList();
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
