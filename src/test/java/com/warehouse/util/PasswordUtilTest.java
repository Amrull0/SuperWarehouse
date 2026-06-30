package com.warehouse.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    // ------------------------------------------------------------------ //
    //  Тест 5: хэш одного пароля каждый раз разный (соль уникальна)      //
    // ------------------------------------------------------------------ //
    @Test
    @DisplayName("Два хэша одного пароля не совпадают (разная соль)")
    void hash_samePassword_producesDistinctHashes() {
        String hash1 = PasswordUtil.hash("секретный123");
        String hash2 = PasswordUtil.hash("секретный123");

        assertNotEquals(hash1, hash2, "Хэши должны различаться из-за разной соли");
    }

    // ------------------------------------------------------------------ //
    //  Тест 6: verify возвращает true для правильного пароля              //
    // ------------------------------------------------------------------ //
    @Test
    @DisplayName("verify → true для верного пароля")
    void verify_correctPassword_returnsTrue() {
        String password = "Admin123!";
        String hash = PasswordUtil.hash(password);

        assertTrue(PasswordUtil.verify(password, hash));
    }

    // ------------------------------------------------------------------ //
    //  Тест 7: verify возвращает false для неверного пароля               //
    // ------------------------------------------------------------------ //
    @Test
    @DisplayName("verify → false для неверного пароля")
    void verify_wrongPassword_returnsFalse() {
        String hash = PasswordUtil.hash("правильный");

        assertFalse(PasswordUtil.verify("неправильный", hash));
    }
}
