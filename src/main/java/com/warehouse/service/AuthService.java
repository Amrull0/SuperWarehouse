package com.warehouse.service;

import com.warehouse.dao.UserDao;
import com.warehouse.model.User;
import com.warehouse.model.UserRole;
import com.warehouse.util.PasswordUtil;

public class AuthService {

    private final UserDao userDao = new UserDao();

    public User register(String username, String password, String confirmPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Введите логин");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Введите пароль");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Пароль должен содержать минимум 6 символов");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }
        if (userDao.findByUsername(username.trim()) != null) {
            throw new IllegalArgumentException("Пользователь уже существует");
        }
        return userDao.create(username.trim(), PasswordUtil.hash(password), UserRole.USER);
    }

    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Введите логин и пароль");
        }
        String hash = userDao.findPasswordHashByUsername(username.trim());
        if (hash == null || !PasswordUtil.verify(password, hash)) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }
        User user = userDao.findByUsername(username.trim());
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        return user;
    }
}