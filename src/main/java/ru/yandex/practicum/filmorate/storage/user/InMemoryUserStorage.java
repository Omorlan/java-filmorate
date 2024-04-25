package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    private Long filmId = 0L;

    private Long getNextId() {
        return ++filmId;
    }

    public List<User> findAll() {
        return users.values().stream().toList();
    }

    public User create(User user) {
        try {
            log.info("Creating new user");
            userValidation(user);
            // save the new user in the application memory
            user.setId(getNextId());
            log.info("Set id = " + user.getId() + " to new user");
            user.setFriends(new HashSet<>());
            users.put(user.getId(), user);
            log.info("User created");
            return user;
        } catch (ValidationException exception) {
            log.error("Error occurred while creating user", exception);
            throw exception;
        }
    }

    public User update(User user) {
        log.info("Updating user with id " + user.getId());
        if (users.containsKey(user.getId())) {
            userValidation(user);
            // if the user is found and all conditions are met, update its content
            User oldUser = users.get(user.getId());
            user.setFriends(oldUser.getFriends());
            users.put(user.getId(), user);
            log.info("User with ID " + user.getId() + " updated successfully");
            return user;
        } else {
            throw new NotFoundException("User with id = " + user.getId() + " not found");
        }

    }

    public void remove(Long id) {
        log.info("Removing user with Id " + id);
        if (users.containsKey(id)) {
            users.remove(id);
        } else {
            throw new NotFoundException("User with id = " + id + " not found");
        }

    }

    public void userValidation(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        for (User existingUser : users.values()) {
            if (existingUser.getEmail().equals(user.getEmail()) && !Objects.equals(existingUser.getId(), user.getId())) {
                String errorMessage = "This email is already in use";
                log.error(errorMessage);
                throw new ValidationException(errorMessage);
            }
        }
    }

    public User getUserById(Long id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            throw new NotFoundException("User with id = " + id + " not found");
        }
    }

    public Map<Long, User> getUsersMap() {
        return users;
    }

}
