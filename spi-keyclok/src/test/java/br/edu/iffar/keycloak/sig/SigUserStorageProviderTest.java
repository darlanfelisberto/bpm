package br.edu.iffar.keycloak.sig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SigUserStorageProviderTest {
    private final UUID userId = UUID.fromString("744324e2-7705-4fa0-9232-886963ff27e7");
    private final RealmModel realm = mock(RealmModel.class);
    private final KeycloakSession session = mock(KeycloakSession.class);
    private final ComponentModel component = mock(ComponentModel.class);
    private final SigUserRepository users = mock(SigUserRepository.class);
    private SigUserStorageProvider provider;

    @BeforeEach
    void setUp() {
        when(component.getId()).thenReturn("sig-component");
        provider = new SigUserStorageProvider(session, component, users);
    }

    @Test
    void returnsStableIdAndExternalProfile() {
        when(users.findByUsername("12345")).thenReturn(
                new SigUserRepository.SigUser(userId, "12345", "sig@example.org", false, "Maria Silva"));
        UserModel user = provider.getUserByUsername(realm, "12345");

        assertEquals(StorageId.keycloakId(component, userId.toString()), user.getId());
        assertEquals("Maria", user.getFirstName());
        assertEquals("Silva", user.getLastName());
        assertEquals("sig@example.org", user.getEmail());
        assertEquals("Maria", user.getFirstAttribute("firstName"));
        assertEquals("sig@example.org", user.getAttributes().get("email").getFirst());
        assertTrue(user.isEnabled());
        assertNull(provider.getUserByEmail(realm, "sig@example.org"));
    }

    @Test
    void rejectsInactiveAccountEvenWhenPasswordMatches() {
        UserModel user = federatedUser();
        CredentialInput input = password("abc");
        when(users.findCredential(userId)).thenReturn(
                new SigUserRepository.SigCredential("900150983cd24fb0d6963f7d28e17f72", true));

        assertFalse(provider.isConfiguredFor(realm, user, PasswordCredentialModel.TYPE));
        assertFalse(provider.isValid(realm, user, input));
    }

    @Test
    void checksCurrentPasswordAndRejectsLocalPasswordChanges() {
        UserModel user = federatedUser();
        when(users.findCredential(userId)).thenReturn(
                new SigUserRepository.SigCredential("900150983cd24fb0d6963f7d28e17f72", false));

        assertTrue(provider.isValid(realm, user, password("abc")));
        assertFalse(provider.isValid(realm, user, password("bad")));
        assertThrows(ReadOnlyException.class, () -> provider.updateCredential(realm, user, password("new")));
    }

    @Test
    void refusesMissingOrForeignUsers() {
        assertNull(provider.getUserByUsername(realm, "missing"));
        UserModel foreign = mock(UserModel.class);
        when(foreign.getId()).thenReturn("f:other:" + userId);
        assertFalse(provider.isValid(realm, foreign, password("abc")));
    }

    private UserModel federatedUser() {
        UserModel user = mock(UserModel.class);
        String id = StorageId.keycloakId(component, userId.toString());
        when(user.getId()).thenReturn(id);
        return user;
    }

    private CredentialInput password(String value) {
        CredentialInput input = mock(CredentialInput.class);
        when(input.getType()).thenReturn(PasswordCredentialModel.TYPE);
        when(input.getChallengeResponse()).thenReturn(value);
        return input;
    }
}
