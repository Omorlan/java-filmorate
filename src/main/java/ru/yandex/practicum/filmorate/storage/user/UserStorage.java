package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;

@Repository
public interface UserStorage {
    User getUserById(Long id);

    Map<Long, User> getUsersMap();

    List<User> findAll();

    User create(User user);

    void remove(Long id);

    User update(User user);
}