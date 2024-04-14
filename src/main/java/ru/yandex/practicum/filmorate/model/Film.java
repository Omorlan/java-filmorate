package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.controller.validation.film.releasedate.FilmReleaseDate;

import java.time.LocalDate;

/**
 * Film.
 */
@Builder
@Data
public class Film {
    private Long id;
    @NotBlank(message = "Film title cannot be empty")
    private String name;
    @Size(max = 200, message = "The string length must not exceed 200 characters")
    private String description;
    @FilmReleaseDate
    private LocalDate releaseDate;
    @Positive(message = "Value must be positive")
    private Long duration;
}
