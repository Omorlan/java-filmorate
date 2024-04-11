package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import java.time.LocalDate;

@Data
public class User {
    Long id;
    @NotBlank(message = "Email address cannot be empty")
    @Email(message = "Incorrect email address format")
    String email;
    @NotBlank(message = "Login cannot be empty")
    String login;
    String name;
    @Past(message = "Date of birth cannot be in the future")
    LocalDate birthday;
}

