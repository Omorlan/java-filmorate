package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.EventMapper;
import ru.yandex.practicum.filmorate.model.event.Event;

import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FeedStorageDb implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void create(Event event) {
        try {
            log.info("Creating event: {}", event);
            KeyHolder keyHolder = new GeneratedKeyHolder();
            final String sqlQuery = """
                    INSERT INTO events (
                    user_id, timestamp, event_type, operation, entity_id
                    ) VALUES (?, ?, ?, ?, ?)
                    """;

            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"event_id"});
                stmt.setLong(1, event.getUserId());
                stmt.setLong(2, event.getTimestamp());
                stmt.setString(3, event.getEventType().name());
                stmt.setString(4, event.getOperation().name());
                stmt.setLong(5, event.getEntityId());
                return stmt;
            }, keyHolder);
        } catch (ValidationException e) {
            log.error("Validation error while creating event: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<Event> get(Long userId) {
        try {
            if (userId == null || userId <= 0) {
                throw new NotFoundException("User with id = " + userId + " not found");
            }
            log.info("Fetching feed for user with id {}", userId);
            final String sqlQuery = """
                    SELECT event_id, user_id, timestamp, event_type, operation, entity_id
                    FROM events
                    WHERE user_id = ?
                    """;
            List<Event> feed = jdbcTemplate.query(sqlQuery, EventMapper::makeEvent, userId);
            log.info("Fetched {} events", feed.size());
            return feed;
        } catch (NotFoundException e) {
            log.error("Not found error while fetching feed for user with id {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
}
