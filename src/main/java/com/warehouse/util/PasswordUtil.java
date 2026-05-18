package com.warehouse.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {

    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private PasswordUtil() {
    }

    public static String hash(String password) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);
            byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Password hashing error: " + e.getMessage(), e);
        }
    }

    public static boolean verify(String password, String stored) {
        try {
            String[] parts = stored.split(":");
            if (parts.length != 2) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);
            byte[] actual = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            if (actual.length != expected.length) {
                return false;
            }
            int result = 0;
            for (int i = 0; i < actual.length; i++) {
                result |= actual[i] ^ expected[i];
            }
            return result == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }
}