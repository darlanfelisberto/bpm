package br.edu.iffar.keycloak.sig;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

final class LegacySigPassword {
    private LegacySigPassword() {
    }

    static boolean matches(String password, String storedHash) {
        if (password == null || storedHash == null || !storedHash.matches("[0-9a-f]{32}")) {
            return false;
        }
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(password.getBytes(StandardCharsets.UTF_8));
            byte[] actual = HexFormat.of().formatHex(digest).getBytes(StandardCharsets.US_ASCII);
            return MessageDigest.isEqual(actual, storedHash.getBytes(StandardCharsets.US_ASCII));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 unavailable for legacy SIG verification", e);
        }
    }
}
