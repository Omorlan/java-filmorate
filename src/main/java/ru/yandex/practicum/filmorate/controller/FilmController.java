package ru.yandex.practicum.filmorate.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DataFormatException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private static final int MAX_DESC_LENGTH = 200;
    private static final LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Retrieving all films");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Creating new film");
        // check necessary conditions
        userValidation(film);
        // generate additional data
        film.setId(getNextId());
        // save the new film in the application memory
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film updatedFilm) {
        log.info("Updating film with ID {}", updatedFilm.getId());
        // check necessary conditions
        try {
            if (updatedFilm.getId() == null) {
                throw new ConditionsNotMetException("The ID must be specified");
            }
            if (films.containsKey(updatedFilm.getId())) {
                userValidation(updatedFilm);
                films.put(updatedFilm.getId(), updatedFilm);
                return updatedFilm;
            }
            throw new NotFoundException("Film with id = " + updatedFilm.getId() + " not found");
        } catch (Exception exception) {
            log.error("Error occurred while updating film", exception);
            throw exception;
        }
    }

    public void userValidation(Film film) {
        log.debug("Validating film: {}", film);
        if (film.getDescription().length() > MAX_DESC_LENGTH) {
            String errorMessage =
                    String.format("The title of the film is more than %d", MAX_DESC_LENGTH);
            log.error(errorMessage);
            throw new DataFormatException(errorMessage);
        }
        if (film.getReleaseDate().isBefore(MIN_DATE)) {
            String errorMessage =
                    String.format("The release date cannot be earlier than %s", MIN_DATE);
            log.error(errorMessage);
            throw new DataFormatException(errorMessage);
        }
    }

    // auxiliary method for generating the identifier of a new user
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
