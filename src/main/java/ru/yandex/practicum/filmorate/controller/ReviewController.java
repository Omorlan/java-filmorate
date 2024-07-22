package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.controller.validation.Marker;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;


    @PostMapping()
    public Review create(@Validated(Marker.Create.class) @RequestBody Review review) throws ValidationException {
        return reviewService.create(review);
    }

    @PutMapping()
    public Review update(@Validated(Marker.Update.class) @RequestBody Review review) throws ValidationException {
        return reviewService.update(review);
    }

    @DeleteMapping("/{id}")
    public void remove(@Validated(Marker.Delete.class) @PathVariable Long id) {
        reviewService.remove(id);
    }

    @GetMapping("/{id}")
    public Review getReview(@PathVariable Long id) {
        return reviewService.getReview(id);
    }


    @GetMapping
    public List<Review> getReviewsByFilm(
            @RequestParam(name = "filmId", required = false) Long filmId,
            @RequestParam(name = "count", defaultValue = "10", required = false) Integer count
    ) {
        return reviewService.getReviewsByFilm(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") Long reviewId, @PathVariable("userId") Long userId) {
        reviewService.addLike(reviewId, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable("id") Long reviewId, @PathVariable("userId") Long userId) {
        reviewService.addDislike(reviewId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable("id") Long reviewId, @PathVariable("userId") Long userId) {
        reviewService.removeLike(reviewId, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable("id") Long reviewId, @PathVariable("userId") Long userId) {
        reviewService.removeDislike(reviewId, userId);
    }
}
