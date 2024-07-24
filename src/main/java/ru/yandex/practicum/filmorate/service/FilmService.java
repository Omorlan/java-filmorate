package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.OperationType;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorageDb;
    private final UserStorage userStorageDb;
    private final DirectorStorage directorStorage;
    private final FeedService feedService;

    public Film getFilm(Long id) {
        return filmStorageDb.getFilmById(id);
    }

    public Film create(Film film) {
        return filmStorageDb.create(film);
    }

    public Film update(Film film) {
        return filmStorageDb.update(film);
    }

    public void delete(Long id) {
        filmStorageDb.delete(id);
    }


    public List<Film> findAll() {
        return filmStorageDb.findAll();
    }

    public void addLike(Long filmId, Long userId) {
        userStorageDb.getUserById(userId);
        filmStorageDb.addLike(filmId, userId);
        feedService.createEvent(
                userId,
                EventType.LIKE,
                OperationType.ADD,
                filmId
        );
    }

    public void removeLike(Long filmId, Long userId) {
        userStorageDb.getUserById(userId);
        filmStorageDb.deleteLike(filmId, userId);
        feedService.createEvent(
                userId,
                EventType.LIKE,
                OperationType.REMOVE,
                filmId
        );
    }

    public List<Film> getPopularFilms(Long count, Long genreId, Integer year) {
        return filmStorageDb.getPopularFilms(count, genreId, year);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        userStorageDb.getUserById(userId);
        userStorageDb.getUserById(friendId);
        return filmStorageDb.getCommonFilms(userId, friendId);
    }

    public List<Film> getByDirector(int directorId, String sortBy) {
        directorStorage.getById(directorId);
        return filmStorageDb.getByDirector(directorId, sortBy);
    }

    public List<Film> searchByTitle(String query) {
        return filmStorageDb.searchByTitle(query);
    }

    public List<Film> searchByDirector(String query) {
        return filmStorageDb.searchByDirector(query);
    }

    public List<Film> serchByTitleAndDirector(String query) {
        return filmStorageDb.searchByTitleAndDirector(query);
    }
}
