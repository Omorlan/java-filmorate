package ru.yandex.practicum.filmorate.controller.validation.film.releasedate;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.controller.validation.constant.Constants;

import java.time.LocalDate;

public class FilmReleaseDateValidation implements ConstraintValidator<FilmReleaseDate, LocalDate> {

    @Override
    public boolean isValid(LocalDate releaseDate, ConstraintValidatorContext constraintValidatorContext) {
        return releaseDate.isAfter(Constants.MIN_DATE);
    }
}