package br.edu.iffar.keycloak.sig;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

final class SigUserAdapter extends AbstractUserAdapterFederatedStorage {
    private final SigUserRepository.SigUser user;
    private final ComponentModel component;

    SigUserAdapter(KeycloakSession session, RealmModel realm, ComponentModel component,
                   SigUserRepository.SigUser user) {
        super(session, realm, component);
        this.component = component;
        this.user = user;
    }

    @Override
    public String getId() {
        return StorageId.keycloakId(component, user.id().toString());
    }

    @Override
    public String getUsername() {
        return user.username();
    }

    @Override
    public String getEmail() {
        return user.email();
    }

    @Override
    public String getFirstName() {
        if (user.fullName() == null) {
            return null;
        }
        String name = user.fullName().trim();
        int space = name.indexOf(' ');
        return space < 0 ? name : name.substring(0, space);
    }

    @Override
    public String getLastName() {
        if (user.fullName() == null) {
            return null;
        }
        String name = user.fullName().trim();
        int space = name.indexOf(' ');
        return space < 0 ? null : name.substring(space + 1);
    }

    @Override
    public boolean isEnabled() {
        return !user.inactive();
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> attributes = new HashMap<>();
        addAttribute(attributes, "username", getUsername());
        addAttribute(attributes, "email", getEmail());
        addAttribute(attributes, "firstName", getFirstName());
        addAttribute(attributes, "lastName", getLastName());
        return attributes;
    }

    @Override
    public String getFirstAttribute(String name) {
        List<String> values = getAttributes().get(name);
        return values == null ? null : values.getFirst();
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        List<String> values = getAttributes().get(name);
        return values == null ? Stream.empty() : values.stream();
    }

    @Override
    public void setEnabled(boolean enabled) {
        throw new ReadOnlyException("SIG account status is managed in the source system");
    }

    @Override
    public void setUsername(String username) {
        throw new ReadOnlyException("SIG usernames are managed in the source system");
    }

    @Override
    public void setEmail(String email) {
        throw new ReadOnlyException("SIG email is managed in the source system");
    }

    @Override
    public void setFirstName(String firstName) {
        throw new ReadOnlyException("SIG names are managed in the source system");
    }

    @Override
    public void setLastName(String lastName) {
        throw new ReadOnlyException("SIG names are managed in the source system");
    }

    private static void addAttribute(Map<String, List<String>> attributes, String name, String value) {
        if (value != null && !value.isBlank()) {
            attributes.put(name, List.of(value));
        }
    }
}
