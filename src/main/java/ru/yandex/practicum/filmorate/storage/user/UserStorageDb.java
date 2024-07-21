package ru.yandex.practicum.filmorate.storage.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
@Getter
public class UserStorageDb implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmMapper filmMapper;

    @Override
    public User create(User user) {
        log.info("Creating user with login: {}", user.getLogin());
        if (user.getLogin().contains(" ")) {
            log.error("User login contains spaces: {}", user.getLogin());
            throw new ValidationException("Логин не должен быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
            log.debug("Assigning login as name for user: {}", user.getLogin());
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        final String sqlQuery = """
                INSERT INTO users (user_name, user_login, user_email, user_birthday)
                VALUES (?,?,?,?)
                """;
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getEmail());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.info("User created with id: {}", user.getId());
        return user;
    }

    @Override
    public void remove(Long id) {
        log.info("Removing user with id: {}", id);
        String sqlQuery = "delete from users WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sqlQuery, id);
        if (rowsAffected == 0) {
            log.error("User with id {} not found for removal", id);
            throw new NotFoundException("User id = " + id + " not found for removal");
        }
        log.info("User with id {} removed successfully", id);
    }

    @Override
    public User update(User newUser) {
        log.info("Updating user with id: {}", newUser.getId());
        if (newUser.getId() == null) {
            log.error("User id is not specified for update");
            throw new ValidationException("Id должен быть указан");
        }
        String sqlQuery =
                "UPDATE users SET user_name = ?, user_login = ?, user_email = ?, user_birthday = ? WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(
                sqlQuery,
                newUser.getName(),
                newUser.getLogin(),
                newUser.getEmail(),
                newUser.getBirthday(),
                newUser.getId()
        );
        if (rowsAffected == 0) {
            log.error("User with id {} not found for update", newUser.getId());
            throw new NotFoundException("User id = " + newUser.getId() + " not found for update");
        }
        log.info("User with id {} updated successfully", newUser.getId());
        return getUserById(newUser.getId());
    }

    @Override
    public List<User> findAll() {
        log.info("Fetching all users");
        return jdbcTemplate.query("SELECT * FROM users", UserMapper::makeUser);
    }

    @Override
    public User getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        final String sqlQuery = "SELECT * FROM users WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sqlQuery, UserMapper::makeUser, id);
        if (users.isEmpty()) {
            log.error("User with id {} not found", id);
            throw new NotFoundException("User id = " + id + " not found");
        }
        log.info("User with id {} fetched successfully", id);
        return users.get(0);
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        log.info("Adding friend with id {} for user with id {}", friendId, id);
        getUserById(id);
        getUserById(friendId);
        final String querySelect = """
                SELECT status FROM friendship
                WHERE (accepting_user_id = ? AND requesting_user_id = ?)
                """;

        final String queryInsert = """
                INSERT INTO friendship (accepting_user_id, requesting_user_id, status)
                VALUES (?,?,?)
                """;

        final String queryUpdate = """
                UPDATE friendship
                SET status = 'confirmed'
                WHERE accepting_user_id = ? AND requesting_user_id = ?
                """;

        List<String> existingStatus = jdbcTemplate.queryForList(querySelect, String.class, id, friendId);

        if (existingStatus.isEmpty()) {
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(queryInsert);
                stmt.setLong(1, id);
                stmt.setLong(2, friendId);
                stmt.setString(3, "unconfirmed");
                return stmt;
            });
            log.info("Friend request from user with id {} to user with id {} created", id, friendId);
        } else if (existingStatus.get(0).equals("unconfirmed")) {
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(queryUpdate);
                stmt.setLong(1, id);
                stmt.setLong(2, friendId);
                return stmt;
            });
            log.info("Friend request from user with id {} to user with id {} confirmed", id, friendId);
        }
    }

    @Override
    public void removeFriend(Long id, Long friendId) {
        log.info("Removing friend with id {} for user with id {}", friendId, id);
        getUserById(id);
        getUserById(friendId);
        final String sqlQuery = """
                DELETE FROM friendship AS u
                WHERE accepting_user_id = ? AND requesting_user_id = ?
                """;
        jdbcTemplate.update(sqlQuery, id, friendId);
        log.info("Friend with id {} removed for user with id {}", friendId, id);
    }

    @Override
    public List<User> getUserFriends(Long id) {
        log.info("Fetching friends for user with id {}", id);
        getUserById(id);
        final String sqlQuery = """
                SELECT u.* FROM users AS u
                JOIN friendship AS f ON u.user_id = f.requesting_user_id
                WHERE f.accepting_user_id = ?
                """;
        List<User> friends = jdbcTemplate.query(sqlQuery, UserMapper::makeUser, id);
        log.info("Fetched {} friends for user with id {}", friends.size(), id);
        return friends;
    }

    @Override
    public List<User> getSameFriends(Long id, Long otherId) {
        log.info("Fetching common friends for users with ids {} and {}", id, otherId);
        getUserById(id);
        getUserById(otherId);
        final String query = """
                SELECT u.* FROM users AS u
                JOIN friendship f1 ON u.user_id = f1.requesting_user_id AND f1.accepting_user_id = ?
                JOIN friendship f2 ON u.user_id = f2.requesting_user_id AND f2.accepting_user_id = ?
                """;
        List<User> commonFriends = jdbcTemplate.query(query, UserMapper::makeUser, id, otherId);
        log.info("Fetched {} common friends for users with ids {} and {}", commonFriends.size(), id, otherId);
        return commonFriends;
    }

    @Override
    public List<Film> getRecommendations(Long userId) {
        log.info("Fetching recommendations for user with id {}", userId);
        String sql = """
                WITH temp AS (SELECT COUNT(*)   AS count,
                                     l1.user_id AS user1_id,
                                     l2.user_id AS user2_id
                              FROM likes AS l1
                                       JOIN likes AS l2 ON l1.film_id = l2.film_id
                              WHERE l1.user_id < l2.user_id
                                AND l1.user_id = ?
                              GROUP BY user1_id, user2_id)
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
                WHERE f.film_id IN (SELECT film_id
                                  FROM likes
                                  WHERE user_id IN (SELECT user2_id
                                                    FROM temp
                                                    WHERE count = (SELECT MAX(count)
                                                                   FROM temp))
                                    AND film_id NOT IN (SELECT film_id
                                                        FROM likes
                                                        WHERE user_id = ?));
                """;
        List<Film> result = jdbcTemplate.query(sql, filmMapper, userId, userId);
        log.info("Fetched {} recommendations for user with id {}", result, userId);
        return result;
    }


}
