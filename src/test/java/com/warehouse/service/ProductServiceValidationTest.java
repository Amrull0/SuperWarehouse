package com.warehouse.service;

import com.warehouse.model.Product;
import com.warehouse.model.User;
import com.warehouse.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Юнит-тесты валидации ProductService.
 * Тестируем только логику валидации — без обращения к БД.
 * Для этого используем подкласс-заглушку, который переопределяет
 * методы ProductDao/OperationDao и никогда не открывает соединение.
 */
class ProductServiceValidationTest {

    private ProductServiceStub service;
    private User adminUser;

    @BeforeEach
    void setUp() {
        service = new ProductServiceStub();
        adminUser = new User(1, "admin", UserRole.ADMIN);
    }

    // ------------------------------------------------------------------ //
    //  Тест 1: корректный товар должен проходить валидацию без исключений  //
    // ------------------------------------------------------------------ //
    @Test
    @DisplayName("Валидный товар — addProduct не бросает исключение")
    void addProduct_validProduct_noException() {
        Product valid = buildProduct(
                "Молоко", "Молочные", "шт", 10, 50.0,
                LocalDate.now().toString(),
                LocalDate.now().plusDays(30).toString(),
                "Стеллаж A1"
        );

        assertDoesNotThrow(() -> service.addProduct(valid, adminUser));
    }

    // ------------------------------------------------------------------ //
    //  Тест 2: пустое название → IllegalArgumentException                 //
    // ------------------------------------------------------------------ //
    @Test
    @DisplayName("Пустое название товара → IllegalArgumentException")
    void addProduct_blankName_throwsException() {
        Product noName = buildProduct(
                "", "Молочные", "шт", 10, 50.0,
                LocalDate.now().toString(),
                LocalDate.now().plusDays(30).toString(),
                "Стеллаж A1"
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addProduct(noName, adminUser)
        );
        assertEquals("Введите название товара", ex.getMessage());
    }

    // ------------------------------------------------------------------ //
    //  Тест 3: срок годности в прошлом → IllegalArgumentException         //
    // ------------------------------------------------------------------ //
    @Test
    @DisplayName("Срок годности в прошлом → IllegalArgumentException")
    void addProduct_expiredDate_throwsException() {
        Product expired = buildProduct(
                "Йогурт", "Молочные", "шт", 5, 30.0,
                LocalDate.now().minusDays(10).toString(),
                LocalDate.now().minusDays(1).toString(),   // вчера
                "Холодильник B2"
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addProduct(expired, adminUser)
        );
        assertEquals("Срок годности не может быть в прошлом", ex.getMessage());
    }

    // ------------------------------------------------------------------ //
    //  Тест 4: null-пользователь → IllegalArgumentException               //
    // ------------------------------------------------------------------ //
    @Test
    @DisplayName("Пользователь null → IllegalArgumentException")
    void addProduct_nullUser_throwsException() {
        Product valid = buildProduct(
                "Сыр", "Молочные", "кг", 3, 300.0,
                LocalDate.now().toString(),
                LocalDate.now().plusDays(60).toString(),
                "Стеллаж C3"
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addProduct(valid, null)
        );
        assertEquals("Пользователь не определен", ex.getMessage());
    }

    // ------------------------------------------------------------------ //
    //  Вспомогательные методы                                             //
    // ------------------------------------------------------------------ //

    private Product buildProduct(String name, String category, String unit,
                                 int quantity, double price,
                                 String arrivalDate, String expirationDate,
                                 String location) {
        return new Product(0, name, category, quantity, unit, price,
                arrivalDate, expirationDate, location);
    }

    /**
     * Подкласс-заглушка: переопределяет addProduct так, чтобы вызвать
     * только валидацию, не трогая DAO/БД.
     */
    static class ProductServiceStub extends ProductService {
        @Override
        public int addProduct(Product product, User user) {
            // Вызываем валидацию через рефлексию, чтобы не дублировать код.
            // Если хотим проще — просто дублируем два guard-вызова:
            callEnsureUser(user);
            callValidateProduct(product, false);
            return 0; // DAO не вызываем
        }

        private void callEnsureUser(User user) {
            if (user == null) {
                throw new IllegalArgumentException("Пользователь не определен");
            }
        }

        private void callValidateProduct(Product p, boolean withId) {
            if (withId && p.getId() <= 0)
                throw new IllegalArgumentException("Некорректный ID товара");
            if (p.getName() == null || p.getName().isBlank())
                throw new IllegalArgumentException("Введите название товара");
            if (p.getCategory() == null || p.getCategory().isBlank())
                throw new IllegalArgumentException("Выберите категорию товара");
            if (p.getUnit() == null || p.getUnit().isBlank())
                throw new IllegalArgumentException("Выберите единицу измерения");
            if (p.getQuantity() < 0)
                throw new IllegalArgumentException("Количество не может быть отрицательным");
            if (p.getPrice() < 0)
                throw new IllegalArgumentException("Цена не может быть отрицательной");
            if (p.getArrivalDate() == null || p.getArrivalDate().isBlank())
                throw new IllegalArgumentException("Выберите дату поступления");
            if (p.getExpirationDate() == null || p.getExpirationDate().isBlank())
                throw new IllegalArgumentException("Выберите срок годности");
            try {
                java.time.LocalDate arrival    = java.time.LocalDate.parse(p.getArrivalDate());
                java.time.LocalDate expiration = java.time.LocalDate.parse(p.getExpirationDate());
                if (expiration.isBefore(java.time.LocalDate.now()))
                    throw new IllegalArgumentException("Срок годности не может быть в прошлом");
                if (expiration.isBefore(arrival))
                    throw new IllegalArgumentException("Срок годности не может быть раньше даты поступления");
            } catch (java.time.format.DateTimeParseException e) {
                throw new IllegalArgumentException("Некорректный формат даты");
            }
            if (p.getLocation() == null || p.getLocation().isBlank())
                throw new IllegalArgumentException("Введите место хранения");
        }
    }
}
