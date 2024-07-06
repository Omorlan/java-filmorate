package ru.yandex.practicum.filmorate.controller;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorageDb;
import ru.yandex.practicum.filmorate.storage.user.UserStorageDb;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserControllerTest {

    private FilmStorageDb storage;
    private UserStorageDb userStorage;
    private UserService userService;
    private UserController controller;
    private Validator validator;

    @BeforeEach
    void setUp() {
        userService = new UserService(userStorage);
        controller = new UserController(userService);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    void genDefaultUser(int count) {
        Stream.generate(() -> User.builder()
                        .id(null)
                        .name("User 1")
                        .email("user@ya.ru")
                        .login("userLogin1")
                        .birthday(LocalDate.of(1999, 4, 6))
                        .build())
                .limit(count)
                .forEach(controller::create);
    }

    User genSpecUser(String name, String email, String login, LocalDate birthday) {
        User user = User.builder()
                .id(null)
                .name(name)
                .email(email)
                .login(login)
                .birthday(birthday)
                .build();
        return user;
    }

    @Test
    void findAllShouldReturnAllUsersList() {
        genDefaultUser(1);
        List<User> userList = controller.findAll();
        assertNotNull(userList);
        assertEquals(1, userList.size());
    }

    @Test
    void createShouldCreateUser() {
        genDefaultUser(1);
        List<User> userList = controller.findAll();
        assertNotNull(userList);
        assertEquals(1, userList.size());
    }

    @Test
    void updateShouldUpdateUser() {
        genDefaultUser(1);
        User updatedUser = User.builder()
                .id(1L)
                .name("User 1")
                .email("user@ya.ru")
                .login("userLogin1")
                .birthday(LocalDate.of(1999, 4, 6))
                .build();
        controller.update(updatedUser);
        List<User> userList = controller.findAll();
        assertNotNull(userList);
        assertEquals(1, userList.size());
        assertEquals(updatedUser, userList.get(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "email",
            "test@.ru",
            "",
            "@yandex.com",
            "@pochta@ya.ru"
    })
    void createFilmWithWrongEmailShouldFail(String email) {
        User user = genSpecUser("Oleg", email, "Oleg1999", LocalDate.of(1999, 4, 6));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "O l e g",
            "Oleg Ivanov",
            "Oleg "
    })
    void createFilmWithWrongLoginShouldFail(String login) {
        User user = genSpecUser("Oleg", "pochta@ya.ru", login, LocalDate.of(1999, 4, 6));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    void ifNameIsEmptyLoginShouldBeName() {
        User user = genSpecUser(null, "pochta@ya.ru", "login", LocalDate.of(1999, 4, 6));
        controller.create(user);
        List<User> userList = controller.findAll();
        assertNotNull(userList);
        assertEquals(1, userList.size());
        assertEquals("login", userList.get(0).getName());
    }

    @Test
    void birthdayCantBeInFuture() {
        User user = genSpecUser("Oleg", "pochta@ya.ru", "login", LocalDate.of(2999, 4, 6));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }
}