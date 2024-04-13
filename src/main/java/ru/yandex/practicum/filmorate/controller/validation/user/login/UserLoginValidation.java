package ru.yandex.practicum.filmorate.controller.validation.user.login;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserLoginValidation implements ConstraintValidator<UserLogin, String> {
    @Override
    public boolean isValid(String login, ConstraintValidatorContext constraintValidatorContext) {
        return !login.contains(" ");
    }
}