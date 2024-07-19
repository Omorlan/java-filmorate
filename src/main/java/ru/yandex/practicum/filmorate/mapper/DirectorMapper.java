package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class DirectorMapper {
    public static Director makeDirector(ResultSet rs, int rowNum) throws SQLException {
        return new Director(rs.getInt("director_id"), rs.getString("director_name"));
    }
}
