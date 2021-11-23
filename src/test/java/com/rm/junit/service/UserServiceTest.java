package com.rm.junit.service;


import entity.User;
import org.junit.jupiter.api.Test;
import service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


class UserServiceTest {

    @Test
    void userEmptyIfNoUserAdded() {
        UserService userService = new UserService();
        List<User> users = userService.getAll();
        assertTrue(users.isEmpty());
    }
}
