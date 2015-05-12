package org.kie.server.services.impl.storage;

public interface KieServerStateRepository {

    void store(String serverId, KieServerState kieServerState);

    KieServerState load(String serverId);
}
