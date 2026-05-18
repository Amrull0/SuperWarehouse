package com.warehouse.dao;

import com.warehouse.db.Database;
import com.warehouse.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    public Product findById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Product lookup error: " + e.getMessage(), e);
        }
    }

    public List<Product> findAll(String search, String category, String unit, String sort) {
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(category) LIKE ?)");
            String like = "%" + search.toLowerCase().trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (category != null && !category.isBlank() && !"All".equalsIgnoreCase(category)) {
            sql.append(" AND LOWER(category) = LOWER(?)");
            params.add(category);
        }
        if (unit != null && !unit.isBlank() && !"All".equalsIgnoreCase(unit)) {
            sql.append(" AND LOWER(unit) = LOWER(?)");
            params.add(unit);
        }

        sql.append(" ORDER BY ");
        sql.append(switch (sort == null ? "id_desc" : sort) {
            case "quantity_asc" -> "quantity ASC";
            case "quantity_desc" -> "quantity DESC";
            case "price_asc" -> "price ASC";
            case "price_desc" -> "price DESC";
            case "expiration_asc" -> "date(expiration_date) ASC";
            case "name_asc" -> "name ASC";
            default -> "id DESC";
        });

        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            List<Product> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Product list error: " + e.getMessage(), e);
        }
    }

    public int insert(Product product) {
        String sql = "INSERT INTO products(name, category, quantity, unit, price, arrival_date, expiration_date, location) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillProduct(ps, product, false);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (Exception e) {
            throw new RuntimeException("Product insert error: " + e.getMessage(), e);
        }
    }

    public void update(Product product) {
        String sql = "UPDATE products SET name = ?, category = ?, quantity = ?, unit = ?, price = ?, arrival_date = ?, expiration_date = ?, location = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillProduct(ps, product, true);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Product update error: " + e.getMessage(), e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Product delete error: " + e.getMessage(), e);
        }
    }

    private static void fillProduct(PreparedStatement ps, Product product, boolean withId) throws Exception {
        ps.setString(1, product.getName());
        ps.setString(2, product.getCategory());
        ps.setInt(3, product.getQuantity());
        ps.setString(4, product.getUnit());
        ps.setDouble(5, product.getPrice());
        ps.setString(6, product.getArrivalDate());
        ps.setString(7, product.getExpirationDate());
        ps.setString(8, product.getLocation());
        if (withId) {
            ps.setInt(9, product.getId());
        }
    }

    private static Product map(ResultSet rs) throws Exception {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getInt("quantity"),
                rs.getString("unit"),
                rs.getDouble("price"),
                rs.getString("arrival_date"),
                rs.getString("expiration_date"),
                rs.getString("location")
        );
    }
}