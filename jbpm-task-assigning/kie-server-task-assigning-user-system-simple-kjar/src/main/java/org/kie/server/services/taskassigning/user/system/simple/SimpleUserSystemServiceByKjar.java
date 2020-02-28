package org.kie.server.services.taskassigning.user.system.simple;

/**
 * Dummy extension of the SimpleUserSystemService for showing that a UserSystemService can be loaded from a kjar.
 */
public class SimpleUserSystemServiceByKjar extends SimpleUserSystemService {

    private static final String NAME = "SimpleUserSystemServiceByKjar";

    public SimpleUserSystemServiceByKjar() {
        //SPI constructor
    }

    @Override
    public String getName() {
        return NAME;
    }
}
