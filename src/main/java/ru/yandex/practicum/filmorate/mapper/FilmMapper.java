package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.util.DateUtil.toLocalDate;

@Component
public class FilmMapper implements ResultSetExtractor<List<Film>> {
    @Override
    public List<Film> extractData(ResultSet rs) throws SQLException {
        Map<Long, Film> filmMap = new LinkedHashMap<>();

        while (rs.next()) {
            Long filmId = rs.getLong("film_id");
            Film film = filmMap.computeIfAbsent(filmId, id -> createFilm(rs));

            addGenreIfPresent(rs, film);
            addLikeIfPresent(rs, film);
        }
        return new ArrayList<>(filmMap.values());
    }

    private Film createFilm(ResultSet rs) {
        try {
            Mpa mpa = new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name"));
            return Film.builder()
                    .id(rs.getLong("film_id"))
                    .name(rs.getString("film_name"))
                    .description(rs.getString("film_description"))
                    .duration(rs.getLong("film_duration"))
                    .releaseDate(toLocalDate(rs.getDate("film_releaseDate")))
                    .mpa(mpa)
                    .genres(new LinkedHashSet<>())
                    .likes(new LinkedHashSet<>())
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating Film object from ResultSet", e);
        }
    }

    private void addGenreIfPresent(ResultSet rs, Film film) throws SQLException {
        int genreId = rs.getInt("genre_id");
        if (!rs.wasNull()) {
            String genreName = rs.getString("genre_name");
            Genre genre = new Genre(genreId, genreName);
            film.getGenres().add(genre);
        }
    }

    private void addLikeIfPresent(ResultSet rs, Film film) throws SQLException {
        Long likeUserId = rs.getLong("like_user_id");
        if (!rs.wasNull()) {
            film.getLikes().add(likeUserId);
        }
    }
}
