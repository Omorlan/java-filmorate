package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorageDb;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ContextConfiguration(classes = {UserStorageDb.class, FilmMapper.class})
class UserStorageDbTest {
    private final UserStorageDb userStorage;


    @Test
    void shouldBeCorrectUserStorageDb() {
        userStorage.create(User.builder()
                .email("grinch@yandex.ru")
                .login("Grinch")
                .name("Oleg")
                .birthday(LocalDate.now())
                .build());
        List<User> users = userStorage.findAll();
        assertEquals(1, users.size());
        assertThat(users.get(0)).hasFieldOrPropertyWithValue("id", 1L);

        User user = userStorage.getUserById(1L);
        assertThat(user).hasFieldOrPropertyWithValue("id", 1L);

        userStorage.create(User.builder()
                .email("ivan@rambler.ru")
                .login("Vanya1332")
                .name("Ivan")
                .birthday(LocalDate.now())
                .build());
        List<User> users1 = userStorage.findAll();
        assertEquals(2, users1.size());
        assertThat(users1.get(0)).hasFieldOrPropertyWithValue("id", 1L);
        assertThat(users1.get(1)).hasFieldOrPropertyWithValue("id", 2L);

        userStorage.update(User.builder()
                .id(1L)
                .email("grinch@yandex.ru")
                .login("Grinch")
                .name("NeOleg")
                .birthday(LocalDate.now())
                .build());
        User user1 = userStorage.getUserById(1L);
        assertThat(user1).hasFieldOrPropertyWithValue("id", 1L);
        assertThat(user1).hasFieldOrPropertyWithValue("name", "NeOleg");
    }
}