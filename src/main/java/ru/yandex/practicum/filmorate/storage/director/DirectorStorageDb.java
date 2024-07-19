package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        String sql = "SELECT * FROM directors WHERE director_id = ?";
        List<Director> genres = jdbcTemplate.query(sql, DirectorMapper::makeDirector, id);
        Optional<Director> optionalDirector = genres.stream().findFirst();
        if (optionalDirector.isPresent()) {
            log.info("Film found: {}", optionalDirector.get());
            return optionalDirector.get();
        } else {
            log.error("Director with id {} not found", id);
            throw new NotFoundException("Director id = " + id + " not found");
        }
    }

    @Override
    public Director create(Director director) {
        log.info("Creating director: {}", director);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        String sqlQuery = """
                INSERT INTO directors (director_name)
                VALUES (?)
                """;

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"director_id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);

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
                SET director_name = ?
                WHERE director_id = ?
                """;
        jdbcTemplate.update(sqlQuery, director.getName(), director.getId());

        return director;
    }

    @Override
    public void delete(int id) {
        String sqlQuery = "DELETE FROM directors WHERE director_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }
}
