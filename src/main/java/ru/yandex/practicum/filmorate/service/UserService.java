package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorageDb;

import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorageDb userStorageDb;

    @Autowired
    public UserService(UserStorageDb userStorageDb) {
        this.userStorageDb = userStorageDb;
    }

    public User getUser(Long id) {
        return userStorageDb.getUserById(id);
    }

    public List<User> findAll() {
        return userStorageDb.findAll();
    }

    public User create(User user) {
        return userStorageDb.create(user);
    }

    public void delete(Long id) {
        userStorageDb.delete(id);
    }

    public User update(User user) {
        return userStorageDb.update(user);
    }

    public void addFriend(Long userId, Long targetId) {
        userStorageDb.addFriend(userId, targetId);
        log.info("Friend added successfully");
    }

    public void removeFriend(Long userId, Long targetId) {
        userStorageDb.removeFriend(userId, targetId);
        log.info("Friend removed successfully");
    }

    public List<User> getSameFriends(Long userId, Long targetId) {
        return userStorageDb.getSameFriends(userId, targetId);
    }

    public List<User> getFriends(Long id) {
        return userStorageDb.getUserFriends(id);
    }

    public List<Film> getRecommendations(Long userId) {
        return userStorageDb.getRecommendations(userId);
    }
}
