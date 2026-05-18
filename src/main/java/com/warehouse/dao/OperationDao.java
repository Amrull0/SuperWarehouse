package com.warehouse.dao;

import com.warehouse.db.Database;
import com.warehouse.model.StockOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OperationDao {

    public void insert(StockOperation operation) {
        String sql = """
                INSERT INTO operations(operation_type, product_id, product_name, quantity, unit, unit_price, total_price, operation_date, username, note)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fill(ps, operation);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Operation insert error: " + e.getMessage(), e);
        }
    }

    public List<StockOperation> findAll(String typeFilter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM operations WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (typeFilter != null && !typeFilter.isBlank() && !"ALL".equalsIgnoreCase(typeFilter)) {
            sql.append(" AND operation_type = ?");
            params.add(typeFilter);
        }

        sql.append(" ORDER BY id DESC");

        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            List<StockOperation> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("Operation list error: " + e.getMessage(), e);
        }
    }

    public List<StockOperation> findAll() {
        return findAll(null);
    }

    private static void fill(PreparedStatement ps, StockOperation op) throws Exception {
        ps.setString(1, op.getOperationType());
        ps.setInt(2, op.getProductId());
        ps.setString(3, op.getProductName());
        ps.setInt(4, op.getQuantity());
        ps.setString(5, op.getUnit());
        ps.setDouble(6, op.getUnitPrice());
        ps.setDouble(7, op.getTotalPrice());
        ps.setString(8, op.getOperationDate());
        ps.setString(9, op.getUsername());
        ps.setString(10, op.getNote());
    }

    private static StockOperation map(ResultSet rs) throws Exception {
        return new StockOperation(
                rs.getInt("id"),
                rs.getString("operation_type"),
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getInt("quantity"),
                rs.getString("unit"),
                rs.getDouble("unit_price"),
                rs.getDouble("total_price"),
                rs.getString("operation_date"),
                rs.getString("username"),
                rs.getString("note")
        );
    }
}
