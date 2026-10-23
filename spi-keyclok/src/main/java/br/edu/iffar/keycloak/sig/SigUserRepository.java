package br.edu.iffar.keycloak.sig;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.UUID;

class SigUserRepository {
    private static final String USER_SELECT = """
            select au.auth_user_id, au.username, au.email, au.inativo, p.nome
            from sig.user_sig us
            join auth.auth_user au on au.auth_user_id = us.auth_user_id
            left join auth.pessoas p on p.auth_user_id = au.auth_user_id
            """;

    private final EntityManager entityManager;

    SigUserRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    SigUser findByUsername(String username) {
        return oneUser(USER_SELECT + " where au.username = ?1", username);
    }

    SigUser findById(UUID id) {
        return oneUser(USER_SELECT + " where au.auth_user_id = ?1", id);
    }

    SigCredential findCredential(UUID id) {
        List<?> rows = entityManager.createNativeQuery("""
                select au.password, au.inativo
                from sig.user_sig us
                join auth.auth_user au on au.auth_user_id = us.auth_user_id
                where au.auth_user_id = ?1
                """).setParameter(1, id).setMaxResults(2).getResultList();
        if (rows.size() > 1) {
            throw new IllegalStateException("Duplicate SIG credential record");
        }
        if (rows.isEmpty()) {
            return null;
        }
        Object[] row = (Object[]) rows.getFirst();
        return new SigCredential((String) row[0], (Boolean) row[1]);
    }

    private SigUser oneUser(String sql, Object value) {
        List<?> rows = entityManager.createNativeQuery(sql)
                .setParameter(1, value).setMaxResults(2).getResultList();
        if (rows.size() > 1) {
            throw new IllegalStateException("Duplicate SIG user record");
        }
        if (rows.isEmpty()) {
            return null;
        }
        Object[] row = (Object[]) rows.getFirst();
        return new SigUser((UUID) row[0], (String) row[1], (String) row[2], (Boolean) row[3], (String) row[4]);
    }

    record SigUser(UUID id, String username, String email, boolean inactive, String fullName) {
    }

    record SigCredential(String hash, boolean inactive) {
    }
}
