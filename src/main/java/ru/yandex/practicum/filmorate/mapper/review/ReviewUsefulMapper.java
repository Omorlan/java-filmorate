package ru.yandex.practicum.filmorate.mapper.review;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReviewUsefulMapper {

    public static Long makeUseful(ResultSet rs, int rowNum) throws SQLException {
        return rs.getLong("useful");
    }
}