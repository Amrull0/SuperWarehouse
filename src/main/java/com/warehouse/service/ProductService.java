package com.warehouse.service;

import com.warehouse.dao.OperationDao;
import com.warehouse.dao.ProductDao;
import com.warehouse.db.Database;
import com.warehouse.model.OperationType;
import com.warehouse.model.Product;
import com.warehouse.model.StockOperation;
import com.warehouse.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ProductService {

    private final ProductDao productDao = new ProductDao();
    private final OperationDao operationDao = new OperationDao();

    public List<Product> findProducts(String search, String category, String unit, String sort) {
        return productDao.findAll(search, category, unit, sort);
    }

    public Product getProduct(int id) {
        return productDao.findById(id);
    }

    public int addProduct(Product product, User user) {
        validateProduct(product, false);
        ensureUser(user);
        return productDao.insert(product);
    }

    public void updateProduct(Product product, User user) {
        validateProduct(product, true);
        ensureUser(user);
        if (productDao.findById(product.getId()) == null) {
            throw new IllegalArgumentException("Товар не найден");
        }
        productDao.update(product);
    }

    public void deleteProduct(int productId, User user) {
        ensureUser(user);
        if (productDao.findById(productId) == null) {
            throw new IllegalArgumentException("Товар не найден");
        }
        productDao.delete(productId);
    }

    public void supply(int productId, int quantity, User user) {
        changeQuantityAndLog(productId, quantity, OperationType.SUPPLY, user, "Поступление товара");
    }

    public void sell(int productId, int quantity, User user) {
        changeQuantityAndLog(productId, -quantity, OperationType.SALE, user, "Продажа товара");
    }

    public void writeOff(int productId, int quantity, User user) {
        changeQuantityAndLog(productId, -quantity, OperationType.WRITE_OFF, user, "Списание товара");
    }

    public List<Product> expiringWithinDays(int days) {
        List<Product> all = productDao.findAll(null, null, null, "expiration_asc");
        LocalDate today = LocalDate.now();
        LocalDate border = today.plusDays(days);
        List<Product> result = new ArrayList<>();
        for (Product product : all) {
            try {
                LocalDate expiration = LocalDate.parse(product.getExpirationDate());
                if (!expiration.isBefore(today) && !expiration.isAfter(border)) {
                    result.add(product);
                }
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    public List<StockOperation> findOperations() {
        return operationDao.findAll(null);
    }

    public List<StockOperation> findOperations(String typeFilter) {
        return operationDao.findAll(typeFilter);
    }

    private void changeQuantityAndLog(int productId, int delta, OperationType type, User user, String note) {
        ensureUser(user);
        if (delta == 0) {
            throw new IllegalArgumentException("Количество должно быть больше нуля");
        }
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Product product = loadProduct(connection, productId);
                if (product == null) {
                    throw new IllegalArgumentException("Товар не найден");
                }

                int newQuantity = product.getQuantity() + delta;
                if (newQuantity < 0) {
                    throw new IllegalArgumentException("Недостаточно товара на складе");
                }

                updateQuantity(connection, productId, newQuantity);

                int opQuantity = Math.abs(delta);
                double total = opQuantity * product.getPrice();
                insertOperation(connection, new StockOperation(
                        0,
                        type.name(),
                        product.getId(),
                        product.getName(),
                        opQuantity,
                        product.getUnit(),
                        product.getPrice(),
                        total,
                        LocalDateTime.now().toString(),
                        user.getUsername(),
                        note
                ));

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof IllegalArgumentException) {
                    throw e;
                }
                throw new RuntimeException("Ошибка операции: " + e.getMessage(), e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            throw new RuntimeException("Ошибка базы данных: " + e.getMessage(), e);
        }
    }

    private Product loadProduct(Connection connection, int id) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM products WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
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
    }

    private void updateQuantity(Connection connection, int productId, int quantity) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE products SET quantity = ? WHERE id = ?")) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    private void insertOperation(Connection connection, StockOperation operation) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO operations(operation_type, product_id, product_name, quantity, unit, unit_price, total_price, operation_date, username, note)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """)) {
            ps.setString(1, operation.getOperationType());
            ps.setInt(2, operation.getProductId());
            ps.setString(3, operation.getProductName());
            ps.setInt(4, operation.getQuantity());
            ps.setString(5, operation.getUnit());
            ps.setDouble(6, operation.getUnitPrice());
            ps.setDouble(7, operation.getTotalPrice());
            ps.setString(8, operation.getOperationDate());
            ps.setString(9, operation.getUsername());
            ps.setString(10, operation.getNote());
            ps.executeUpdate();
        }
    }

    private void validateProduct(Product product, boolean withId) {
        if (withId && product.getId() <= 0) {
            throw new IllegalArgumentException("Некорректный ID товара");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Введите название товара");
        }
        if (product.getCategory() == null || product.getCategory().isBlank()) {
            throw new IllegalArgumentException("Выберите категорию товара");
        }
        if (product.getUnit() == null || product.getUnit().isBlank()) {
            throw new IllegalArgumentException("Выберите единицу измерения");
        }
        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException("Количество не может быть отрицательным");
        }
        if (product.getPrice() < 0) {
            throw new IllegalArgumentException("Цена не может быть отрицательной");
        }
        if (product.getArrivalDate() == null || product.getArrivalDate().isBlank()) {
            throw new IllegalArgumentException("Выберите дату поступления");
        }
        if (product.getExpirationDate() == null || product.getExpirationDate().isBlank()) {
            throw new IllegalArgumentException("Выберите срок годности");
        }
        try {
            LocalDate arrival = LocalDate.parse(product.getArrivalDate());
            LocalDate expiration = LocalDate.parse(product.getExpirationDate());
            if (expiration.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Срок годности не может быть в прошлом");
            }
            if (expiration.isBefore(arrival)) {
                throw new IllegalArgumentException("Срок годности не может быть раньше даты поступления");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Некорректный формат даты");
        }
        if (product.getLocation() == null || product.getLocation().isBlank()) {
            throw new IllegalArgumentException("Введите место хранения");
        }
    }

    private void ensureUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не определен");
        }
    }
}
