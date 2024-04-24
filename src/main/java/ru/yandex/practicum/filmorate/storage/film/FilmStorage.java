package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Repository
public interface FilmStorage {
    Film getFilmById(Long id);

    List<Film> findAll();

    Film create(Film film);

    void remove(Long id);

    Film update(Film film);
}