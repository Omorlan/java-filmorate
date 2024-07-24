package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Repository
public interface UserStorage {
    User getUserById(Long id);

    List<User> findAll();

    User create(User user);

    void delete(Long id);

    void addFriend(Long id, Long friendId);

    void removeFriend(Long id, Long friendId);

    User update(User user);

    List<User> getUserFriends(Long id);

    List<User> getSameFriends(Long id, Long otherId);

    List<Film> getRecommendations(Long userId);
}