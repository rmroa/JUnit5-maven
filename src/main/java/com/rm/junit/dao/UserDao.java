package com.rm.junit.dao;

import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DriverManager;

public class UserDao {

    @SneakyThrows
    public boolean delete(Integer userId) {
        Connection connection = DriverManager.getConnection("url", "username", "password");
        return true;
    }
}
