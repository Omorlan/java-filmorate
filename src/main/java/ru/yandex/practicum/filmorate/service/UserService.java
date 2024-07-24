package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.OperationType;
import ru.yandex.practicum.filmorate.storage.user.UserStorageDb;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorageDb userStorageDb;
    private final FeedService feedService;

    public User getUser(Long id) {
        return userStorageDb.getUserById(id);
    }

    public List<User> findAll() {
        return userStorageDb.findAll();
    }

    public User create(User user) {
        return userStorageDb.create(user);
    }

    public void remove(Long id) {
        userStorageDb.remove(id);
    }

    public User update(User user) {
        return userStorageDb.update(user);
    }

    public void addFriend(Long userId, Long targetId) {
        userStorageDb.addFriend(userId, targetId);
        feedService.createEvent(userId, EventType.FRIEND, OperationType.ADD, targetId);
        log.info("Friend added successfully");
    }

    public void removeFriend(Long userId, Long targetId) {
        userStorageDb.removeFriend(userId, targetId);
        feedService.createEvent(userId, EventType.FRIEND, OperationType.REMOVE, targetId);
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
