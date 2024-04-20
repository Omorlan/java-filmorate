package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserControllerTest {

    static UserController userController = new UserController();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @AllArgsConstructor
    static class ExpectedViolation {
        String propertyPath;
        String message;
    }

    @Test
    void validateUserOk() {
        final User user = new User(
                "user@mail.ru",
                "login",
                "name",
                LocalDate.of(2014, 4, 17)
        );
        validator.validate(user);
    }

    @Test
    void validateEmailWithoutDogFail() {
        final User user = new User(
                "user-mail.ru",
                "login",
                "name",
                LocalDate.of(2014, 4, 17)
        );
        List<ConstraintViolation<User>> violations = new ArrayList<>(validator.validate(user));
        assertEquals(1, violations.size());
    }

    @Test
    void validateEmailNullFail() {
        final User user = new User(
                null,
                "login",
                "name",
                LocalDate.of(2014, 4, 17)
        );
        List<ConstraintViolation<User>> violations = new ArrayList<>(validator.validate(user));
        assertEquals(1, violations.size());
    }

    @Test
    void validateLoginNullFail() {
        final User user = new User(
                "user@mail.ru",
                null,
                "name",
                LocalDate.of(2014, 4, 17)
        );
        List<ConstraintViolation<User>> violations = new ArrayList<>(validator.validate(user));
        assertEquals(1, violations.size());
    }


    @Test
    void validateLoginContainsSpaceFail() {
        final User user = new User(
                "user@mail.ru",
                "Login Fail",
                "name",
                LocalDate.of(2014, 4, 17)
        );
        List<ConstraintViolation<User>> violations = new ArrayList<>(validator.validate(user));
        assertEquals(1, violations.size());
    }

    @Test
    void validateNameNullOk() {
        final User user = new User(
                "user@mail.ru",
                "Login",
                null,
                LocalDate.of(2014, 4, 17)
        );
        validator.validate(user);

        assertEquals(user.getName(), user.getLogin());
    }

    @Test
    void validateNameIsBlankOk() {
        final User user = new User(
                "user@mail.ru",
                "Login",
                "",
                LocalDate.of(2014, 4, 17)
        );
        validator.validate(user);

        assertEquals(user.getName(), user.getLogin());
    }

    @Test
    void validateBirthdayAfterNowFail() {
        final User user = new User(
                "user@mail.ru",
                "Login",
                "name",
                LocalDate.of(2025, 4, 17)
        );
        List<ConstraintViolation<User>> violations = new ArrayList<>(validator.validate(user));
        assertEquals(1, violations.size());
    }

    @Test
    void validateBirthdayNowOk() {
        final User user = new User(
                "user@mail.ru",
                "Login",
                "name",
                LocalDate.now()
        );
        validator.validate(user);
    }

    @Test
    void validateBirthdayNullOk() {
        final User user = new User(
                "user@mail.ru",
                "Login",
                "name",
                null
        );
        validator.validate(user);
    }
}