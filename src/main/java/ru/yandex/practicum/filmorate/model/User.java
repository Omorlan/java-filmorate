package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.controller.validation.user.login.UserLogin;

import java.time.LocalDate;
import java.util.Set;

@Builder
@Data
public class User {
    private Long id;
    @NotBlank(message = "Email address cannot be empty")
    @Email(message = "Incorrect email address format")
    private String email;
    @NotBlank(message = "Login cannot be empty")
    @UserLogin
    private String login;
    private String name;
    @Past(message = "Date of birth cannot be in the future")
    private LocalDate birthday;
    private Set<Long> friends;
}

