package com.Eventhub.controller;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.mindrot.jbcrypt.BCrypt;

final class PasswordUtil {

    private static final String LEGACY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String BCRYPT_PREFIX = "bcrypt-sha256$";
    private static final int BCRYPT_COST = 12;
    private static final int KEY_BITS = 256;

    private PasswordUtil() {
    }

    static String hash(String password) throws GeneralSecurityException {
        String prehashed = Base64.getEncoder().encodeToString(
                MessageDigest.getInstance("SHA-256")
                        .digest(password.getBytes(StandardCharsets.UTF_8)));
        return BCRYPT_PREFIX + BCrypt.hashpw(
                prehashed, BCrypt.gensalt(BCRYPT_COST));
    }

    static boolean needsUpgrade(String encoded) {
        return encoded != null && encoded.startsWith("pbkdf2$");
    }

    static boolean verify(String password, String encoded)
            throws GeneralSecurityException {
        if (password == null || encoded == null) {
            return false;
        }
        if (encoded.startsWith(BCRYPT_PREFIX)) {
            String prehashed = Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-256")
                            .digest(password.getBytes(StandardCharsets.UTF_8)));
            try {
                return BCrypt.checkpw(prehashed, encoded.substring(BCRYPT_PREFIX.length()));
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return verifyLegacy(password, encoded);
    }

    private static boolean verifyLegacy(String password, String encoded)
            throws GeneralSecurityException {
        String[] parts = encoded.split("\\$", -1);
        if (parts.length != 4 || !"pbkdf2".equals(parts[0])) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 100_000 || iterations > 1_000_000) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(password, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations)
            throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(LEGACY_ALGORITHM)
                    .generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}
