package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;

import static ru.yandex.practicum.filmorate.util.DateUtil.toLocalDate;

public class UserMapper {
    public static User makeUser(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("user_id"))
                .name(rs.getString("user_name"))
                .login(rs.getString("user_login"))
                .email(rs.getString("user_email"))
                .birthday(toLocalDate(rs.getDate("user_birthday")))
                .build();
    }
}
