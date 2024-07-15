package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class GenreService {
    private GenreStorage genreStorage;

    public Genre getGenreById(long id) {
        return genreStorage.getGenreById(id);
    }

    public List<Genre> getGenres() {
        return genreStorage.getGenres();
    }
}
