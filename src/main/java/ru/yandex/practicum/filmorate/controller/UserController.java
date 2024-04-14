package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public List<User> findAll() {
        return users.values().stream().toList();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        try {
            // check necessary conditions
            userValidation(user);
            // save the new user in the application memory
            user.setId(getNextId());
            users.put(user.getId(), user);
            return user;
        } catch (ValidationException exception) {
            log.error("Error occurred while creating user", exception);
            throw exception;
        }
    }

    @PutMapping
    public User update(@Valid @RequestBody User updatedUser) {
        try {
            if (users.containsKey(updatedUser.getId())) {
                userValidation(updatedUser);
                // if the user is found and all conditions are met, update its content
                users.put(updatedUser.getId(), updatedUser);
                log.debug("User with ID " + updatedUser.getId() + " updated successfully");
                return updatedUser;
            }
            throw new NotFoundException("User with id = " + updatedUser.getId() + " not found");
        } catch (ValidationException | NotFoundException exception) {
            log.error("Error occurred while updating user", exception);
            throw exception;
        }
    }


    public void userValidation(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        for (User existingUser : users.values()) {
            if (existingUser.getEmail().equals(user.getEmail()) && existingUser.getId() != user.getId()) {
                String errorMessage = "This email is already in use";
                log.error(errorMessage);
                throw new ValidationException(errorMessage);
            }
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
