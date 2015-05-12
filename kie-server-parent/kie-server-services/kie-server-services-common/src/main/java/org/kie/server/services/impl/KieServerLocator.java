package org.kie.server.services.impl;

public class KieServerLocator {

    private static final KieServerImpl INSTANCE = new KieServerImpl();

    private KieServerLocator() {
    }

    public static KieServerImpl getInstance() { return INSTANCE; }

}
