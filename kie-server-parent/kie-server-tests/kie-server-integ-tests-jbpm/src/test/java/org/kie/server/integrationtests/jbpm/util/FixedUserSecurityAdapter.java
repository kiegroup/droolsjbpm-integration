package org.kie.server.integrationtests.jbpm.util;

import java.util.ArrayList;
import java.util.List;

import org.kie.server.api.security.SecurityAdapter;

public class FixedUserSecurityAdapter implements SecurityAdapter {

    @Override
    public String getUser() {
        return "yoda";
    }

    @Override
    public List<String> getRoles() {
        List<String> roles = new ArrayList<String>();

        return roles;
    }
}
