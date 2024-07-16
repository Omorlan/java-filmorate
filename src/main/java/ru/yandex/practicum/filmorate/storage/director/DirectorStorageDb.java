package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DirectorStorageDb implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> getAll() {
        log.info("Fetching all directors");
        final String sqlQuery = "SELECT * FROM DIRECTORS";
        return jdbcTemplate.query(sqlQuery, DirectorMapper::makeDirector);
    }

    @Override
    public Director getById(int id) {
        log.info("Fetching director with id: {}", id);
        final String sqlQuery = "SELECT * FROM directors WHERE director_id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, DirectorMapper::makeDirector, id);
    }

    @Override
    public Director create(Director director) {
        log.info("Creating director: {}", director);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        String sqlQuery = """
                INSERT INTO directors (directors_name)
                VALUES (:name)
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", director.getName());
        jdbcTemplate.update(sqlQuery, params, keyHolder);

        Integer id = Objects.requireNonNull(keyHolder.getKeyAs(Integer.class));
        director.setId(id);

        log.info("Director created: {}", director);
        return director;
    }

    @Override
    public Director update(Director director) {
        log.info("Updating director: {}", director);
        String sqlQuery = """
                UPDATE directors
                SET director_name = :name
                WHERE director_id = :directorId
                """;
        Map<String, Object> map = Map.of("directorId", director.getId(), "director_name", director.getName());
        jdbcTemplate.update(sqlQuery, map);

        return director;
    }

    @Override
    public void delete(int id) {
        String sqlQuery = "DELETE FROM directors WHERE director_id = :directorID";
        Map<String, Object> map = Map.of("directorId", id);
        jdbcTemplate.update(sqlQuery, map);
    }
}
