package com.warehouse.dao;

import com.warehouse.db.Database;
import com.warehouse.model.User;
import com.warehouse.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

public class UserDao {

    public User findByUsername(String username) {
        String sql = "SELECT id, username, role FROM users WHERE username = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            UserRole.valueOf(rs.getString("role"))
                    );
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("User query error: " + e.getMessage(), e);
        }
    }

    public String findPasswordHashByUsername(String username) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("password_hash") : null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Password lookup error: " + e.getMessage(), e);
        }
    }

    public User create(String username, String passwordHash, UserRole role) {
        String sql = "INSERT INTO users(username, password_hash, role, created_at) VALUES (?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, role.name());
            ps.setString(4, LocalDateTime.now().toString());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                int id = keys.next() ? keys.getInt(1) : -1;
                return new User(id, username, role);
            }
        } catch (Exception e) {
            throw new RuntimeException("User creation error: " + e.getMessage(), e);
        }
    }
}