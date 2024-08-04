package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmStorageDb implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmMapper filmMapper;

    @Override
    public Film create(Film film) {
        log.info("Creating film: {}", film);
        if (film.getMpa().getId() > 5) {
            log.error("Invalid MPA id: {}", film.getMpa().getId());
            throw new ValidationException("Неверно задан MPA");
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        final String sqlQuery = """
                INSERT INTO films (film_name, film_description, film_duration, film_releaseDate, mpa_id)
                VALUES (?,?,?,?,?)
                """;
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setLong(3, film.getDuration());
            stmt.setDate(4, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        long filmId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(filmId);

        addFilmGenres(film);
        addFilmDirectors(film);
        log.info("Film created: {}", film);
        return film;
    }

    @Override
    public void delete(Long id) {
        log.info("Removing film with id: {}", id);
        String sqlQuery = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, id);
        log.info("Film with id {} removed", id);
    }

    @Override
    public Film update(Film newFilm) {
        log.info("Updating film: {}", newFilm);
        if (newFilm.getId() == null) {
            log.error("Film id is missing during update");
            throw new ValidationException("The ID must be specified");
        }
        final String sqlQuery = """
                UPDATE films
                SET film_name = ?, film_description = ?, film_duration = ?, film_releaseDate = ?, mpa_id = ?
                WHERE film_id = ?
                """;
        jdbcTemplate.update(
                sqlQuery,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getDuration(),
                newFilm.getReleaseDate(),
                newFilm.getMpa().getId(),
                newFilm.getId()
        );

        String removeGenresQuery = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(removeGenresQuery, newFilm.getId());

        String removeDirectorsQuery = "DELETE FROM film_directors WHERE film_id = ?";
        jdbcTemplate.update(removeDirectorsQuery, newFilm.getId());

        addFilmGenres(newFilm);
        addFilmDirectors(newFilm);

        Film updatedFilm = getFilmById(newFilm.getId());
        log.info("Film updated: {}", updatedFilm);
        return updatedFilm;
    }

    @Override
    public List<Film> findAll() {
        log.info("Fetching all films");
        final String sqlQuery = """
                    SELECT f.*,
                           m.mpa_id, m.mpa_name,
                           g.genre_id, g.genre_name,
                           l.user_id AS like_user_id,
                           d.director_id, d.director_name
                    FROM films f
                    JOIN mpa m ON f.mpa_id = m.mpa_id
                    LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                    LEFT JOIN genres g ON fg.genre_id = g.genre_id
                    LEFT JOIN likes l ON f.film_id = l.film_id
                    LEFT JOIN film_directors fd ON f.film_id = fd.film_id
                    LEFT JOIN directors d ON fd.director_id = d.director_id
                """;
        List<Film> films = jdbcTemplate.query(sqlQuery, filmMapper);
        log.info("Fetched {} films", films.size());
        return films;
    }

    @Override
    public Film getFilmById(Long id) {
        log.info("Fetching film with id: {}", id);
        List<Film> films = findAll();
        Optional<Film> optionalFilm = films.stream()
                .filter(f -> f.getId().equals(id))
                .findFirst();

        if (optionalFilm.isPresent()) {
            log.info("Film found: {}", optionalFilm.get());
            return optionalFilm.get();
        } else {
            log.error("Film with id {} not found", id);
            throw new NotFoundException("Film id = " + id + " not found");
        }
    }

    @Override
    public List<Film> getByDirector(int directorId, String sortBy) {
        LinkedHashMap<Long, Film> films = "year".equals(sortBy) ? sortByYear(directorId) : sortByLikes(directorId);

        setGenresAndDirectors(films);
        return films.values().stream().toList();
    }

    @Override
    public List<Film> searchByTitle(String query) {
        log.info("Search by title: {}", query);
        final String sqlQuery = """
                SELECT f.*,
                           m.mpa_id, m.mpa_name,
                           g.genre_id, g.genre_name,
                           l.user_id AS like_user_id,
                           d.director_id, d.director_name
                FROM FILMS f
                JOIN mpa m ON f.mpa_id = m.mpa_id
                LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                LEFT JOIN genres g ON fg.genre_id = g.genre_id
                LEFT JOIN likes l ON f.film_id = l.film_id
                LEFT JOIN film_directors fd ON f.film_id = fd.film_id
                LEFT JOIN directors d ON fd.director_id = d.director_id
                JOIN (
                    SELECT ff.FILM_ID, COUNT(l.USER_ID) count_like
                    FROM LIKES l
                    RIGHT JOIN films ff ON l.FILM_ID = ff.FILM_ID
                    GROUP BY ff.FILM_ID
                    ORDER BY COUNT(l.USER_ID) DESC
                    ) t ON t.FILM_ID = f.FILM_ID
                WHERE LOWER(f.FILM_NAME) LIKE ?
                ORDER BY t.count_like DESC
                """;
        List<Film> films = jdbcTemplate.query(sqlQuery, filmMapper, "%" + query.toLowerCase() + "%");
        log.info("Found {} films", films.size());
        return films;
    }

    @Override
    public List<Film> searchByDirector(String query) {
        log.info("Search by director: {}", query);
        final String sqlQuery = """
                SELECT f.*,
                           m.mpa_id, m.mpa_name,
                           g.genre_id, g.genre_name,
                           l.user_id AS like_user_id,
                           d.director_id, d.director_name
                FROM FILMS f
                JOIN mpa m ON f.mpa_id = m.mpa_id
                LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                LEFT JOIN genres g ON fg.genre_id = g.genre_id
                LEFT JOIN likes l ON f.film_id = l.film_id
                LEFT JOIN film_directors fd ON f.film_id = fd.film_id
                LEFT JOIN directors d ON fd.director_id = d.director_id
                JOIN (
                    SELECT ff.FILM_ID, COUNT(l.USER_ID) count_like
                    FROM LIKES l
                    RIGHT JOIN films ff ON l.FILM_ID = ff.FILM_ID
                    GROUP BY ff.FILM_ID
                    ORDER BY COUNT(l.USER_ID) DESC
                    ) t ON t.FILM_ID = f.FILM_ID
                WHERE LOWER(d.DIRECTOR_NAME) LIKE ?
                ORDER BY t.count_like DESC
                """;
        List<Film> films = jdbcTemplate.query(sqlQuery, filmMapper, "%" + query.toLowerCase() + "%");
        log.info("Found {} films", films.size());
        return films;
    }

    @Override
    public List<Film> searchByTitleAndDirector(String query) {
        log.info("Search by title and director: {}", query);
        final String sqlQuery = """
                SELECT f.*,
                           m.mpa_id, m.mpa_name,
                           g.genre_id, g.genre_name,
                           l.user_id AS like_user_id,
                           d.director_id, d.director_name
                FROM FILMS f
                JOIN mpa m ON f.mpa_id = m.mpa_id
                LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                LEFT JOIN genres g ON fg.genre_id = g.genre_id
                LEFT JOIN likes l ON f.film_id = l.film_id
                LEFT JOIN film_directors fd ON f.film_id = fd.film_id
                LEFT JOIN directors d ON fd.director_id = d.director_id
                JOIN (
                    SELECT ff.FILM_ID, COUNT(l.USER_ID) count_like
                    FROM LIKES l
                    RIGHT JOIN films ff ON l.FILM_ID = ff.FILM_ID
                    GROUP BY ff.FILM_ID
                    ORDER BY COUNT(l.USER_ID) DESC
                    ) t ON t.FILM_ID = f.FILM_ID
                WHERE LOWER(d.DIRECTOR_NAME) LIKE ?
                OR LOWER(f.FILM_NAME) LIKE ?
                ORDER BY t.count_like DESC
                """;
        List<Film> films = jdbcTemplate.query(sqlQuery, filmMapper, "%" + query.toLowerCase() + "%",
                "%" + query.toLowerCase() + "%");
        log.info("Found {} films", films.size());
        return films;
    }

    @Override
    public void addLike(Long id, Long userId) {
        log.info("Adding like to film with id: {} from user with id: {}", id, userId);
        getFilmById(id);
        deleteLike(id, userId);
        final String sqlQuery = """
                INSERT INTO likes (film_id, user_id)
                VALUES(?,?)
                """;
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery);
            stmt.setLong(1, id);
            stmt.setLong(2, userId);
            return stmt;
        });
        log.info("Like added to film with id: {} from user with id: {}", id, userId);
    }

    @Override
    public void deleteLike(Long id, Long userId) {
        log.info("Deleting like from film with id: {} by user with id: {}", id, userId);
        final String sqlQuery = """
                DELETE FROM likes
                WHERE film_id = ? AND user_id = ?
                """;
        jdbcTemplate.update(sqlQuery, id, userId);
        log.info("Like deleted from film with id: {} by user with id: {}", id, userId);
    }

    @Override
    public List<Film> getPopularFilms(Long count, Long genreId, Integer year) {
        log.info("Fetching top {} popular films", count);
        final String sqlQuery = """
                SELECT f.*,
                 m.mpa_id, m.mpa_name,
                 g.genre_id, g.genre_name,
                 l.user_id AS like_user_id,
                 d.director_id, d.director_name
                 FROM films f
                 JOIN mpa m ON f.mpa_id = m.mpa_id
                 LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                 LEFT JOIN genres g ON fg.genre_id = g.genre_id
                 LEFT JOIN likes l ON f.film_id = l.film_id
                 LEFT JOIN film_directors fd ON f.film_id = fd.film_id
                 LEFT JOIN directors d ON fd.director_id = d.director_id
                 JOIN (
                 SELECT ff.FILM_ID, COUNT(l.USER_ID) count_like
                 		FROM LIKES l
                 		RIGHT JOIN films ff ON l.FILM_ID = ff.FILM_ID
                 		%s
                 		GROUP BY ff.FILM_ID
                 		ORDER BY COUNT(l.USER_ID) DESC
                 		LIMIT ?
                 ) t ON t.FILM_ID = f.FILM_ID
                 ORDER BY t.count_like DESC
                """;
        String condition = " ";
        List<Object> params = new ArrayList<>();
        if (genreId != null) {
            condition = condition + " JOIN FILM_GENRES fg ON fg.FILM_ID = l.FILM_ID AND fg.GENRE_ID = ? ";
            params.add(genreId);
        }
        if (year != null) {
            condition = condition + " JOIN films f ON l.FILM_ID = f.FILM_ID AND YEAR(f.FILM_RELEASEDATE) = ? ";
            params.add(year);
        }
        params.add(count);
        String sql = String.format(sqlQuery, condition);

        List<Film> popularFilms = jdbcTemplate.query(sql, filmMapper, params.toArray());
        log.info("Fetched {} popular films", popularFilms.size());
        return popularFilms;
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        String query = """
                WITH temp AS (SELECT l1.film_id AS common_films
                              FROM likes AS l1
                                       INNER JOIN likes AS l2 ON l1.film_id = l2.film_id
                              WHERE l1.user_id = ?
                                AND l2.user_id = ?)
                SELECT f.*,
                       m.mpa_id, m.mpa_name,
                       g.genre_id, g.genre_name,
                       l.user_id AS like_user_id,
                       d.director_id, d.director_name
                FROM films f
                    INNER JOIN temp ON temp.common_films = f.film_id
                         JOIN mpa m ON f.mpa_id = m.mpa_id
                         LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                         LEFT JOIN genres g ON fg.genre_id = g.genre_id
                         LEFT JOIN likes l ON f.film_id = l.film_id
                         LEFT JOIN film_directors fd ON f.film_id = fd.film_id
                         LEFT JOIN directors d ON fd.director_id = d.director_id;
                """;
        return jdbcTemplate.query(query, filmMapper, userId, friendId);
    }

    private void addFilmGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            final String genreInsertQuery = """
                    INSERT INTO film_genres (film_id, genre_id)
                    VALUES (?, ?)
                    """;
            jdbcTemplate.batchUpdate(genreInsertQuery, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Genre genre = (Genre) film.getGenres().toArray()[i];
                    if (genre.getId() > 6) {
                        log.error("Invalid genre id: {}", genre.getId());
                        throw new ValidationException("Такого жанра с id = " + genre.getId() + " нет");
                    }
                    ps.setLong(1, film.getId());
                    ps.setLong(2, genre.getId());
                }

                @Override
                public int getBatchSize() {
                    return film.getGenres().size();
                }
            });
        }
    }

    private void addFilmDirectors(Film film) {
        Set<Director> directors = film.getDirectors();
        if (directors == null || directors.isEmpty()) {
            return;
        }

        String sqlQuery = "INSERT INTO film_directors (film_id, director_id) " +
                "VALUES (?, ?);";

        jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Director director = (Director) film.getDirectors().toArray()[i];
                if (director == null) {
                    return;
                }
                ps.setLong(1, film.getId());
                ps.setLong(2, director.getId());
            }

            @Override
            public int getBatchSize() {
                return film.getDirectors().size();
            }
        });
    }

    private void setGenresAndDirectors(Map<Long, Film> films) {

        Map<Integer, Genre> genres = jdbcTemplate.query("SELECT * FROM genres", GenreMapper::makeGenre).stream()
                .collect(Collectors.toMap(Genre::getId, Function.identity()));

        Map<Long, Director> directors = jdbcTemplate.query("SELECT * FROM directors", DirectorMapper::makeDirector)
                .stream()
                .collect(Collectors.toMap(Director::getId, Function.identity()));

        jdbcTemplate.query("SELECT * FROM film_genres", rs -> {

            Film film = null;
            while (rs.next()) {
                film = films.get(rs.getLong("film_id"));
                if (film != null) {
                    Genre genre = genres.get(rs.getInt("genre_id"));
                    film.getGenres().add(genre);
                }
            }
            return film;
        });

        jdbcTemplate.query("SELECT * FROM film_directors;", rs -> {

            Film film = null;
            while (rs.next()) {
                film = films.get(rs.getLong("film_id"));
                if (film != null) {
                    Director director = directors.get(rs.getLong("director_id"));
                    film.getDirectors().add(director);
                }
            }
            return film;
        });
    }

    private LinkedHashMap<Long, Film> sortByYear(int directorId) {
        String sql = """
                SELECT f.*,
                           m.mpa_id, m.mpa_name,
                           g.genre_id, g.genre_name,
                           l.user_id AS like_user_id,
                           d.director_id, d.director_name
                    FROM films f
                    JOIN mpa m ON f.mpa_id = m.mpa_id
                    LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                    LEFT JOIN genres g ON fg.genre_id = g.genre_id
                    LEFT JOIN likes l ON f.film_id = l.film_id
                    LEFT JOIN film_directors fd ON f.film_id = fd.film_id
                    LEFT JOIN directors d ON fd.director_id = d.director_id
                WHERE fd.director_id = ?
                GROUP BY f.film_id
                ORDER BY f.film_releaseDate
                """;
        return jdbcTemplate.query(sql, filmMapper, directorId)
                .stream()
                .collect(Collectors.toMap(Film::getId, Function.identity(),
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));
    }

    private LinkedHashMap<Long, Film> sortByLikes(int directorId) {
        String getFilmsByLikes = """
                SELECT f.*,
                           m.mpa_id, m.mpa_name,
                           g.genre_id, g.genre_name,
                           l.user_id as like_user_id,
                           d.director_id, d.director_name,
                           COUNT(*) as film_likes
                    FROM films f
                    JOIN mpa m ON f.mpa_id = m.mpa_id
                    LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                    LEFT JOIN genres g ON fg.genre_id = g.genre_id
                    LEFT JOIN likes l ON f.film_id = l.film_id
                    LEFT JOIN film_directors fd ON f.film_id = fd.film_id
                    LEFT JOIN directors d ON fd.director_id = d.director_id
                    LEFT JOIN (SELECT film_id, count(*) likes_count
                    FROM likes GROUP BY film_id) fl ON fl.film_id = f.film_id
                WHERE fd.director_id = ?
                GROUP BY f.film_id, l.user_id
                ORDER BY fl.likes_count DESC""";
        return Objects.requireNonNull(jdbcTemplate.query(getFilmsByLikes, filmMapper, directorId)).stream()
                .collect(Collectors.toMap(Film::getId, Function.identity(),
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));
    }

}
