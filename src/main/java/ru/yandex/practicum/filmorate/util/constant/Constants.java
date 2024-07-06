package ru.yandex.practicum.filmorate.util.constant;

import java.time.LocalDate;

public class Constants {
    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);
}
