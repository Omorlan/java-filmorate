package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FilmControllerTest {
    private InMemoryFilmStorage storage;
    private InMemoryUserStorage userStorage;
    private FilmService filmService;
    private FilmController controller;
    private Validator validator;

    @BeforeEach
    void setUp() {
        storage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmService = new FilmService(storage, userStorage);
        controller = new FilmController(filmService);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    void genDefaultFilm(int count) {
        Stream.generate(() -> Film.builder()
                        .id(null)
                        .name("Film")
                        .description("FilmDesc")
                        .releaseDate(LocalDate.of(1998, 11, 11))
                        .duration(40L)
                        .build())
                .limit(count)
                .forEach(controller::create);
    }

    Film genSpecFilm(String name, String desc, LocalDate release, Long duration) {
        Film film = Film.builder()
                .id(null)
                .name(name)
                .description(desc)
                .releaseDate(release)
                .duration(duration)
                .build();
        return film;
    }


    @Test
    void findAllShouldReturnAllFilmsList() {
        genDefaultFilm(2);
        List<Film> filmList = controller.findAll();
        assertNotNull(filmList);
        assertEquals(2, filmList.size());
    }

    @Test
    void createShouldCreateFilm() {
        genDefaultFilm(1);
        List<Film> filmList = controller.findAll();
        assertNotNull(filmList);
        assertEquals(1, filmList.size());
    }

    @Test
    void updateShouldUpdateFilm() {
        genDefaultFilm(1);
        Film updatedFilm = Film.builder()
                .id(1L)
                .name("UpdatedFilm")
                .description("FilmDesc")
                .releaseDate(LocalDate.of(1998, 11, 11))
                .duration(40L)
                .build();
        controller.update(updatedFilm);
        List<Film> filmList = controller.findAll();
        assertNotNull(filmList);
        assertEquals(1, filmList.size());
        assertEquals(updatedFilm, filmList.get(0));
    }

    @Test
    void createFilmWithTooLongDescriptionShouldFail() {
        String desc = "a".repeat(201);
        Film film = genSpecFilm("film", desc, LocalDate.of(1998, 11, 11), 20L);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void createFilmWithTooLateReleaseDateShouldFail() {
        LocalDate releaseDate = LocalDate.of(1800, 11, 11);
        Film film = genSpecFilm("film", "film desc", releaseDate, 20L);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(longs = {
            0,
            -1
    })
    void createFilmWithNonPositiveDurationShouldFail(Long duration) {
        Film film = genSpecFilm("film", "film desc", LocalDate.of(1998, 11, 11), duration);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " "
    })
    void createFilmWithEmptyNameShouldFail(String name) {
        Film film = genSpecFilm(name, "film desc", LocalDate.of(1998, 11, 11), 20L);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }
}