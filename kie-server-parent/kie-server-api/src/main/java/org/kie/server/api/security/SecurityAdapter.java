package org.kie.server.api.security;

import java.util.List;

public interface SecurityAdapter {

    String getUser();

    List<String> getRoles();
}
