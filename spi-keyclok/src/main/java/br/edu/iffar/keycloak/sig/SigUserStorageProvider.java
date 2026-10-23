package br.edu.iffar.keycloak.sig;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;

import java.util.UUID;
import java.util.stream.Stream;

public final class SigUserStorageProvider implements UserStorageProvider, UserLookupProvider,
        CredentialInputValidator, CredentialInputUpdater {
    private static final System.Logger LOG = System.getLogger(SigUserStorageProvider.class.getName());
    private final KeycloakSession session;
    private final ComponentModel component;
    private final SigUserRepository users;

    SigUserStorageProvider(KeycloakSession session, ComponentModel component, SigUserRepository users) {
        this.session = session;
        this.component = component;
        this.users = users;
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        UUID externalId = externalId(id);
        return externalId == null ? null : adapt(realm, users.findById(externalId));
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        return username == null ? null : adapt(realm, users.findByUsername(username));
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        return null;
    }

    @Override
    public boolean supportsCredentialType(String type) {
        return PasswordCredentialModel.TYPE.equals(type);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String type) {
        UUID id = supportsCredentialType(type) ? externalId(user.getId()) : null;
        SigUserRepository.SigCredential credential = id == null ? null : users.findCredential(id);
        return credential != null && !credential.inactive() && credential.hash() != null;
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (input == null || !supportsCredentialType(input.getType()) || input.getChallengeResponse() == null) {
            return false;
        }
        UUID id = externalId(user.getId());
        SigUserRepository.SigCredential credential = id == null ? null : users.findCredential(id);
        return credential != null && !credential.inactive()
                && LegacySigPassword.matches(input.getChallengeResponse(), credential.hash());
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        if (input != null && supportsCredentialType(input.getType())) {
            throw new ReadOnlyException("SIG passwords must be changed in the source system");
        }
        return false;
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String type) {
        if (supportsCredentialType(type)) {
            throw new ReadOnlyException("SIG passwords must be changed in the source system");
        }
    }

    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
        return Stream.empty();
    }

    @Override
    public void close() {
    }

    private UserModel adapt(RealmModel realm, SigUserRepository.SigUser user) {
        return user == null ? null : new SigUserAdapter(session, realm, component, user);
    }

    private UUID externalId(String id) {
        if (id == null || !component.getId().equals(StorageId.providerId(id))) {
            return null;
        }
        try {
            return UUID.fromString(StorageId.externalId(id));
        } catch (IllegalArgumentException e) {
            LOG.log(System.Logger.Level.WARNING, "Invalid SIG federated user ID", e);
            return null;
        }
    }
}
