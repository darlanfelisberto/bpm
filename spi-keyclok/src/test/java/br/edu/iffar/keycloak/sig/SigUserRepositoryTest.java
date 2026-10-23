package br.edu.iffar.keycloak.sig;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SigUserRepositoryTest {
    @Test
    void rejectsAmbiguousUsername() {
        EntityManager entityManager = mock(EntityManager.class);
        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(1, "duplicated")).thenReturn(query);
        when(query.setMaxResults(2)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new Object[5], new Object[5]));

        assertThrows(IllegalStateException.class,
                () -> new SigUserRepository(entityManager).findByUsername("duplicated"));
    }

    @Test
    void returnsNoUserWhenLookupIsEmpty() {
        EntityManager entityManager = mock(EntityManager.class);
        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(1, "missing")).thenReturn(query);
        when(query.setMaxResults(2)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        assertNull(new SigUserRepository(entityManager).findByUsername("missing"));
    }

    @Test
    void propagatesDatabaseFailureInsteadOfAuthenticating() {
        EntityManager entityManager = mock(EntityManager.class);
        when(entityManager.createNativeQuery(anyString()))
                .thenThrow(new PersistenceException("database unavailable"));

        assertThrows(PersistenceException.class,
                () -> new SigUserRepository(entityManager).findByUsername("12345"));
    }
}
