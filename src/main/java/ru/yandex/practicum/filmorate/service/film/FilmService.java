package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class FilmService {
    private final FilmStorage inMemoryFilmStorage;
    private final UserStorage inMemoryUserStorage;

    @Autowired
    public FilmService(InMemoryFilmStorage inMemoryFilmStorage, InMemoryUserStorage inMemoryUserStorage) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    public Film getFilm(Long id) {
        return inMemoryFilmStorage.getFilmById(id);
    }

    public Film create(Film film) {
        return inMemoryFilmStorage.create(film);
    }

    public Film update(Film film) {
        return inMemoryFilmStorage.update(film);
    }

    public User getUser(Long id) {
        return inMemoryUserStorage.getUserById(id);
    }

    public List<Film> findAll() {
        return inMemoryFilmStorage.findAll();
    }

    public void addLike(Long filmId, Long userId) {
        Film film = getFilm(filmId);
        User user = getUser(userId);
        Set<Long> filmLikes = film.getLikes();
        filmLikes.add(user.getId());
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = getFilm(filmId);
        User user = getUser(userId);
        Set<Long> filmLikes = film.getLikes();
        filmLikes.remove(user.getId());
    }

    public List<Film> getTenMostPopularFilms(int count) {
        return inMemoryFilmStorage.findAll().stream()
                .sorted(Comparator.comparingLong(film -> film.getLikes().size() * -1))
                .limit(count)
                .toList();
    }
}
