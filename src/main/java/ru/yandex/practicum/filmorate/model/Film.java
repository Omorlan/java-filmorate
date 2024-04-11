package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * Film.
 */
@Data
public class Film {
    Long id;
    @NotBlank(message = "Film title cannot be empty")
    String name;
    String description;
    LocalDate releaseDate;
    @Min(value = 1, message = "Value must be positive")
    Long duration;
}
