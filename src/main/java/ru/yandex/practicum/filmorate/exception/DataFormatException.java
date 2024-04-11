package ru.yandex.practicum.filmorate.exception;

public class DataFormatException extends RuntimeException {
    public DataFormatException(String message) {
        super(message);
    }
}