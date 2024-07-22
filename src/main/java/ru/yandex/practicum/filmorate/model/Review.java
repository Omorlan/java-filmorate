package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.controller.validation.Marker;

@Data
@Builder
public class Review {
    @Null(groups = Marker.Create.class, message = "When creating, you cannot specify an id")
    @NotNull(groups = Marker.Update.class)
    @Positive(groups = Marker.Update.class, message = "Update target id must be > 0")
    @NotNull(groups = Marker.Delete.class)
    @Positive(groups = Marker.Delete.class, message = "Delete target id must be > 0")
    private Long reviewId;
    @NotBlank
    @Size(max = 1024)
    private String content;
    @NotNull
    private Boolean isPositive;
    @NotNull
    @Positive
    private Long userId;
    @NotNull
    @Positive
    private Long filmId;
    private Long useful;
}
