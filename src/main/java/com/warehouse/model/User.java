package com.warehouse.model;

public class User {
    private final int id;
    private final String username;
    private final UserRole role;

    public User(int id, String username, UserRole role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}