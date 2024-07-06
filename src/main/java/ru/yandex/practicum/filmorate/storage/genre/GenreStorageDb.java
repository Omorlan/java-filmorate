package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class GenreStorageDb implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getGenres() {
        log.info("Fetching all genres");
        final String sqlQuery = "SELECT * FROM genres";
        List<Genre> genres = jdbcTemplate.query(sqlQuery, GenreMapper::makeGenre);
        log.info("Fetched {} genres", genres.size());
        return genres;
    }

    @Override
    public Genre getGenreById(long id) {
        log.info("Fetching genre with id: {}", id);
        final String sqlQuery = "SELECT * FROM genres WHERE genre_id = ?";

        final List<Genre> genres = jdbcTemplate.query(sqlQuery, GenreMapper::makeGenre, id);
        if (genres.size() != 1) {
            log.error("Genre with id {} not found", id);
            throw new NotFoundException("Genre id = " + id + " not found");
        }
        Genre genre = genres.get(0);
        log.info("Fetched genre: {}", genre);
        return genre;
    }
}
