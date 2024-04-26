package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private Long filmId = 0L;

    private Long getNextId() {
        return ++filmId;
    }

    public List<Film> findAll() {
        log.info("Retrieving all films");
        return films.values().stream().toList();
    }


    public Film create(Film film) {
        log.info("Creating new film");
        film.setId(getNextId());
        log.info("Set id = " + film.getId() + " to new film");
        film.setLikes(new HashSet<>());
        films.put(film.getId(), film);
        log.info("Film created");
        return film;
    }


    public Film update(Film updatedFilm) throws NoSuchElementException {
        log.info("Updating film with ID {}", updatedFilm.getId());
        // check necessary conditions
        if (films.containsKey(updatedFilm.getId())) {
            Film oldFilm = films.get(updatedFilm.getId());
            updatedFilm.setLikes(oldFilm.getLikes());
            films.put(updatedFilm.getId(), updatedFilm);
            return updatedFilm;
        } else {
            throw new NotFoundException("Film with id = " + updatedFilm.getId() + " not found");
        }
    }

    public void remove(Long id) {
        if (films.containsKey(id)) {
            films.remove(id);
        } else {
            throw new NotFoundException("Film with id = " + id + " not found");
        }
    }

    public Film getFilmById(Long id) {
        if (films.containsKey(id)) {
            return films.get(id);
        } else {
            throw new NotFoundException("Film with id = " + id + " not found");
        }
    }
}