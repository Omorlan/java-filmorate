package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MpaService {
    private MpaStorage dbMpaStorage;

    public Mpa getMpaById(long id) {
        return dbMpaStorage.getMpaById(id);
    }

    public List<Mpa> getMpa() {
        return dbMpaStorage.getMpa();
    }
}
