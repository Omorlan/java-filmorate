package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorageDb;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorageDb;

import java.util.List;

@Service
public class FilmService {
    private final FilmStorage filmStorageDb;
    private final UserStorage userStorageDb;
    private final DirectorStorage directorStorage;

    @Autowired
    public FilmService(FilmStorageDb filmStorageDb, UserStorageDb userStorageDb, DirectorStorage directorStorage) {
        this.filmStorageDb = filmStorageDb;
        this.userStorageDb = userStorageDb;
        this.directorStorage = directorStorage;
    }

    public Film getFilm(Long id) {
        return filmStorageDb.getFilmById(id);
    }

    public Film create(Film film) {
        return filmStorageDb.create(film);
    }

    public Film update(Film film) {
        return filmStorageDb.update(film);
    }

    public void remove(Long id) {
        filmStorageDb.remove(id);
    }


    public List<Film> findAll() {
        return filmStorageDb.findAll();
    }

    public void addLike(Long filmId, Long userId) {
        userStorageDb.getUserById(userId);
        filmStorageDb.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        userStorageDb.getUserById(userId);
        filmStorageDb.deleteLike(filmId, userId);
    }

    public List<Film> getPopularFilms(Long count) {
        return filmStorageDb.getPopularFilms(count);
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
