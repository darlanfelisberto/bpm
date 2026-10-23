package br.edu.iffar.keycloak.sig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacySigPasswordTest {
    @Test
    void verifiesLegacySigDigest() {
        String hash = "900150983cd24fb0d6963f7d28e17f72";
        assertTrue(LegacySigPassword.matches("abc", hash));
        assertFalse(LegacySigPassword.matches("abd", hash));
        assertFalse(LegacySigPassword.matches("abc", null));
        assertFalse(LegacySigPassword.matches("abc", "not-a-md5-hash"));
    }
}
