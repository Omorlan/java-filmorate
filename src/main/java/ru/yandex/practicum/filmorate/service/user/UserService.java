package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
public class UserService {
    private final UserStorage inMemoryUserStorage;

    @Autowired
    public UserService(InMemoryUserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    public User getUser(Long id) {
        return inMemoryUserStorage.getUserById(id);
    }

    public List<User> findAll() {
        return inMemoryUserStorage.findAll();
    }

    public User create(User user) {
        return inMemoryUserStorage.create(user);
    }

    public User update(User user) {
        return inMemoryUserStorage.update(user);
    }

    public void addFriend(Long userId, Long targetId) {
        if (Objects.equals(userId, targetId)) {
            log.warn("Attention! the IDs cannot match");
            throw new IllegalArgumentException();
        } else {
            log.info(String.format("Adding to friends. " +
                    "User with id = %s adds user with id = %s", userId, targetId));
            User user = getUser(userId);
            User friend = getUser(targetId);
            Set<Long> userFriends = user.getFriends();
            Set<Long> targetFriends = friend.getFriends();
            userFriends.add(targetId);
            targetFriends.add(userId);
            log.info("Friend added successfully");
        }
    }

    public void removeFriend(Long userId, Long targetId) {
        User user = getUser(userId);
        User friend = getUser(targetId);
        Set<Long> userFriends = user.getFriends();
        Set<Long> targetFriends = friend.getFriends();
        userFriends.remove(targetId);
        targetFriends.remove(userId);
        log.info("Friend removed successfully");
    }

    public List<User> getSameFriends(Long userId, Long targetId) {
        User user = getUser(userId);
        User targetUser = getUser(targetId);
        Set<Long> userFriends = user.getFriends();
        Set<Long> targetFriends = targetUser.getFriends();
        //List for same friends
        List<User> sameFriends = new ArrayList<>();

        userFriends.stream()
                .filter(targetFriends::contains)
                .map(inMemoryUserStorage::getUserById)
                .forEach(sameFriends::add);
        return sameFriends;
    }

    public List<User> getUserFriends(Long id) {
        User user = inMemoryUserStorage.getUserById(id);
        Set<Long> friendsIds = user.getFriends();
        Map<Long, User> users = inMemoryUserStorage.getUsersMap();
        return friendsIds.stream()
                .filter(users::containsKey)
                .map(users::get)
                .toList();
    }


}
