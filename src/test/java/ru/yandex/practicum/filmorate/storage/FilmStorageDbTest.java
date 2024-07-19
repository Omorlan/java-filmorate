package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorageDb;
import ru.yandex.practicum.filmorate.storage.user.UserStorageDb;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ContextConfiguration(classes = {FilmStorageDb.class, UserStorageDb.class, FilmMapper.class})
class FilmStorageDbTest {
    private final FilmStorageDb filmStorageDb;

    @Test
    void shouldBeCorrectfilmStorageDb() {
        filmStorageDb.create(Film.builder()
                .name("test")
                .description("testDescription")
                .duration(120L)
                .releaseDate(LocalDate.now())
                .mpa(new Mpa(1, "G"))
                .build()
        );
        List<Film> films = filmStorageDb.findAll();
        assertEquals(1, films.size());
        assertThat(films.get(0)).hasFieldOrPropertyWithValue("id", 1L);

        Film film = filmStorageDb.getFilmById(1L);
        assertThat(film).hasFieldOrPropertyWithValue("id", 1L);

        filmStorageDb.create(Film.builder()
                .name("Film1")
                .description("film1 Description2")
                .duration(121L)
                .releaseDate(LocalDate.now())
                .mpa(new Mpa(1, "PG-13"))
                .build()
        );
        List<Film> films1 = filmStorageDb.findAll();
        assertEquals(2, films1.size());
        assertThat(films1.get(0)).hasFieldOrPropertyWithValue("id", 1L);
        assertThat(films1.get(1)).hasFieldOrPropertyWithValue("id", 2L);

        filmStorageDb.update(Film.builder()
                .id(1L)
                .name("Bronson")
                .description("Film about Bronson")
                .duration(80L)
                .releaseDate(LocalDate.now())
                .mpa(new Mpa(1, "PG-13"))
                .build()
        );
        Film film1 = filmStorageDb.getFilmById(1L);
        assertThat(film1).hasFieldOrPropertyWithValue("id", 1L);
        assertThat(film1).hasFieldOrPropertyWithValue("name", "Bronson");
    }
}