package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class MpaStorageDb implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> getMpa() {
        log.info("Fetching all MPAs");
        final String sqlQuery = "SELECT * FROM mpa";
        List<Mpa> mpaList = jdbcTemplate.query(sqlQuery, MpaMapper::makeMpa);
        log.info("Fetched {} MPAs", mpaList.size());
        return mpaList;
    }

    @Override
    public Mpa getMpaById(long id) {
        log.info("Fetching MPA with id: {}", id);
        final String sqlQuery = "SELECT * FROM mpa WHERE mpa_id = ?";
        final List<Mpa> mpaList = jdbcTemplate.query(sqlQuery, MpaMapper::makeMpa, id);
        if (mpaList.size() != 1) {
            log.error("MPA with id {} not found", id);
            throw new NotFoundException("MPA id = " + id + " not found");
        }
        Mpa mpa = mpaList.get(0);
        log.info("Fetched MPA: {}", mpa);
        return mpa;
    }

}
