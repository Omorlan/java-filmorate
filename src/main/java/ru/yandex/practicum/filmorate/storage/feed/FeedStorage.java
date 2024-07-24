package ru.yandex.practicum.filmorate.storage.feed;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.event.Event;

import java.util.List;

@Repository
public interface FeedStorage {

    void create(Event event);

    List<Event> get(Long userId);
}
