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

    void addLike(Long id, Long userId);

    void deleteLike(Long id, Long userId);

    List<Film> getPopularFilms(Long count);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> getByDirector(int directorId, String sortBy);

}