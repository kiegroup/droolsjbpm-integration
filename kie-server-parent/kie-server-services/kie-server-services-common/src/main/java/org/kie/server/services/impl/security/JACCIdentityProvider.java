package org.kie.server.services.impl.security;

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;

import org.kie.internal.identity.IdentityProvider;

public class JACCIdentityProvider implements IdentityProvider {

    @Override
    public String getName() {
        Subject subject = getSubjectFromContainer();

        if (subject != null) {
            return subject.getPrincipals().iterator().next().getName();
        }
        return "unknown";
    }

    @Override
    public List<String> getRoles() {
        List<String> roles = new ArrayList<String>();

        Subject subject = getSubjectFromContainer();
        if (subject != null) {
            Set<Principal> principals = subject.getPrincipals();

            if (principals != null) {

                roles = new ArrayList<String>();
                for (Principal principal : principals) {
                    if (principal instanceof Group) {
                        Enumeration<? extends Principal> groups = ((Group) principal).members();

                        while (groups.hasMoreElements()) {
                            Principal groupPrincipal = (Principal) groups.nextElement();
                            roles.add(groupPrincipal.getName());
                        }
                        break;
                    }
                }
            }

        }

        return roles;
    }

    @Override
    public boolean hasRole(String s) {
        return false;
    }

    protected Subject getSubjectFromContainer() {
        try {
            return (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
        } catch (Exception e) {
            return null;
        }
    }
}
