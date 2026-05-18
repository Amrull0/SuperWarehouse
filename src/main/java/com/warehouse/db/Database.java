package com.warehouse.db;

import com.warehouse.util.PasswordUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

public final class Database {

    private static final String APP_DIR = System.getProperty("user.home") + "/.superwarehouse";
    private static final String DB_FILE = APP_DIR + "/warehouse.db";
    private static final String URL = "jdbc:sqlite:" + DB_FILE;

    private Database() {
    }

    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(URL);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
            return connection;
        } catch (Exception e) {
            throw new RuntimeException("Database connection error: " + e.getMessage(), e);
        }
    }

    public static void init() {
        try {
            Path dir = Paths.get(APP_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {

                statement.execute("""
                        CREATE TABLE IF NOT EXISTS users (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT NOT NULL UNIQUE,
                            password_hash TEXT NOT NULL,
                            role TEXT NOT NULL CHECK(role IN ('ADMIN','USER')),
                            created_at TEXT NOT NULL
                        )
                        """);

                statement.execute("""
                        CREATE TABLE IF NOT EXISTS products (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            category TEXT NOT NULL,
                            quantity INTEGER NOT NULL CHECK(quantity >= 0),
                            unit TEXT NOT NULL,
                            price REAL NOT NULL CHECK(price >= 0),
                            arrival_date TEXT NOT NULL,
                            expiration_date TEXT NOT NULL,
                            location TEXT NOT NULL
                        )
                        """);

                statement.execute("""
                        CREATE TABLE IF NOT EXISTS operations (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            operation_type TEXT NOT NULL CHECK(operation_type IN ('SUPPLY','SALE','WRITE_OFF')),
                            product_id INTEGER NOT NULL,
                            product_name TEXT NOT NULL,
                            quantity INTEGER NOT NULL CHECK(quantity > 0),
                            unit TEXT NOT NULL,
                            unit_price REAL NOT NULL CHECK(unit_price >= 0),
                            total_price REAL NOT NULL CHECK(total_price >= 0),
                            operation_date TEXT NOT NULL,
                            username TEXT NOT NULL,
                            note TEXT
                        )
                        """);

                statement.execute("CREATE INDEX IF NOT EXISTS idx_products_name ON products(name)");
                statement.execute("CREATE INDEX IF NOT EXISTS idx_products_category ON products(category)");
                statement.execute("CREATE INDEX IF NOT EXISTS idx_products_expiration ON products(expiration_date)");
                statement.execute("CREATE INDEX IF NOT EXISTS idx_operations_type ON operations(operation_type)");
                statement.execute("CREATE INDEX IF NOT EXISTS idx_operations_date ON operations(operation_date)");
            }

            ensureSeedAdmin();
        } catch (Exception e) {
            throw new RuntimeException("Database initialization error: " + e.getMessage(), e);
        }
    }

    private static void ensureSeedAdmin() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) AS cnt FROM users")) {
            if (rs.next() && rs.getInt("cnt") == 0) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO users(username, password_hash, role, created_at) VALUES (?, ?, ?, ?)")) {
                    ps.setString(1, "admin");
                    ps.setString(2, PasswordUtil.hash("Admin123!"));
                    ps.setString(3, "ADMIN");
                    ps.setString(4, LocalDateTime.now().toString());
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Admin seed error: " + e.getMessage(), e);
        }
    }
}