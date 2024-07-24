package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {
    List<Director> getAll();

    Director getById(long id);

    Director create(Director director);

    Director update(Director director);

    void delete(int id);
}
