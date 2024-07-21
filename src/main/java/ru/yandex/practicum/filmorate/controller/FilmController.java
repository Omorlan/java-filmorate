package ru.yandex.practicum.filmorate.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> findAll() {
        log.info("Retrieving all films");
        return filmService.findAll();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) throws ValidationException {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film updatedFilm) throws ValidationException {
        return filmService.update(updatedFilm);
    }

    @DeleteMapping("/{id}")
    public void deleteFilmById(@PathVariable Long id) {
        filmService.remove(id);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        return filmService.getFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(value = "count", defaultValue = "10") Long count) {
        return filmService.getPopularFilms(count);
    }


    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam(value = "userId") @Positive Long userId, @RequestParam(value = "friendId") @Positive Long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getByDirector(@PathVariable int directorId, @RequestParam String sortBy) {
        return filmService.getByDirector(directorId, sortBy);
    }

    @GetMapping("/search")
    public List<Film> search(@RequestParam(value = "query") String query, @RequestParam(value = "by") String by) {
        switch (by) {
            case "title":
                return filmService.searchByTitle(query);
            case "director":
                return filmService.searchByDirector(query);
            case "title,director":
            case "director,title":
                return filmService.serchByTitleAndDirector(query);
            default:
                throw new ValidationException("Invalid parameter value");
        }
    }
}
