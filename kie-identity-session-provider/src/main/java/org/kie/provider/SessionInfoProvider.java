package org.kie.provider;

import org.jboss.errai.security.shared.api.identity.User;

public interface SessionInfoProvider {

    String getId();

    User getIdentity();
}
