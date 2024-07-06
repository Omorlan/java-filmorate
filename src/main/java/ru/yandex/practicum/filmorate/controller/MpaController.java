package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/mpa")
public class MpaController {
    private MpaService mpaService;

    @GetMapping
    public List<Mpa> getMpa() {
        return mpaService.getMpa();
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable("id") Long id) {
        return mpaService.getMpaById(id);
    }
}
