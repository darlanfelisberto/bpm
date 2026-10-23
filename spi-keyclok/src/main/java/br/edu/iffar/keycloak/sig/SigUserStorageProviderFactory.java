package br.edu.iffar.keycloak.sig;

import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

public final class SigUserStorageProviderFactory implements UserStorageProviderFactory<SigUserStorageProvider> {
    public static final String ID = "sig-postgres";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getHelpText() {
        return "Read-only SIG users from the Sistema-DCLC PostgreSQL database";
    }

    @Override
    public SigUserStorageProvider create(KeycloakSession session, ComponentModel component) {
        var connection = session.getProvider(JpaConnectionProvider.class, "sigstore");
        if (connection == null) {
            throw new IllegalStateException("SIG datasource 'sigstore' is unavailable");
        }
        return new SigUserStorageProvider(session, component,
                new SigUserRepository(connection.getEntityManager()));
    }
}
