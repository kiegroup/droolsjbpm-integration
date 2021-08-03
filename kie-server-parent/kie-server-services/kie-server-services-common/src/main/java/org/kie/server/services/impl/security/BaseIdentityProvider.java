package org.kie.server.services.impl.security;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Stack;

import org.kie.internal.identity.IdentityProvider;
import org.kie.server.api.security.SecurityAdapter;

public abstract class BaseIdentityProvider
        implements IdentityProvider {

    protected Stack<String> contextUsers;

    protected static final ServiceLoader<SecurityAdapter> securityAdapters = ServiceLoader.load(SecurityAdapter.class);

    protected List<SecurityAdapter> adapters = new ArrayList<>();

    public BaseIdentityProvider() {
        for (SecurityAdapter adapter : securityAdapters) {
            adapters.add(adapter);
        }
        contextUsers = new Stack<>();
    }

    @Override
    public void setContextIdentity(String userId) {
        contextUsers.push(userId);
    }

    @Override
    public void removeContextIdentity() {
        contextUsers.pop();
    }

    protected String getNameFromAdapter() {
        for (SecurityAdapter adapter : adapters) {
            String name = adapter.getUser();
            if (name != null && !name.isEmpty()) {
                return name;
            }
        }

        return "unknown";
    }

    protected List<String> getRolesFromAdapter() {
        List<String> roles = new ArrayList<String>();

        for (SecurityAdapter adapter : adapters) {
            List<String> adapterRoles = adapter.getRoles();
            if (adapterRoles != null && !adapterRoles.isEmpty()) {
                roles.addAll(adapterRoles);
            }
        }

        return roles;
    }
}
