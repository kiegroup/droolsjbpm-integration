package org.kie.server.services.impl.security;

import java.lang.ThreadLocal;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.kie.internal.identity.IdentityProvider;
import org.kie.server.api.security.SecurityAdapter;

public abstract class BaseIdentityProvider
        implements IdentityProvider {

    protected static ThreadLocal<String> contextUsers = new ThreadLocal<String>();

    protected static final ServiceLoader<SecurityAdapter> securityAdapters = ServiceLoader.load(SecurityAdapter.class);

    protected List<SecurityAdapter> adapters = new ArrayList<>();

    public BaseIdentityProvider() {
        for (SecurityAdapter adapter : securityAdapters) {
            adapters.add(adapter);
        }
    }

    @Override
    public void setContextIdentity(String userId) {
        contextUsers.set(userId);
    }

    @Override
    public void removeContextIdentity() {
        contextUsers.remove();
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
