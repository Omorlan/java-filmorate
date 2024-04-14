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
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public List<Film> findAll() {
        log.info("Retrieving all films");
        return films.values().stream().toList();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Creating new film");
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film updatedFilm) {
        log.info("Updating film with ID {}", updatedFilm.getId());
        // check necessary conditions
        try {

            if (films.containsKey(updatedFilm.getId())) {
                films.put(updatedFilm.getId(), updatedFilm);
                return updatedFilm;
            } else {
                throw new NotFoundException("Film with id = " + updatedFilm.getId() + " not found");
            }
        } catch (ValidationException | NotFoundException exception) {
            log.error("Error occurred while updating film", exception);
            throw exception;
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
