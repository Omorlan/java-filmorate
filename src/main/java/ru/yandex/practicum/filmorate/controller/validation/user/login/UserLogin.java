package ru.yandex.practicum.filmorate.controller.validation.user.login;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
        validatedBy = {UserLoginValidation.class}
)
public @interface UserLogin {
    String message() default "Login contains spaces";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}