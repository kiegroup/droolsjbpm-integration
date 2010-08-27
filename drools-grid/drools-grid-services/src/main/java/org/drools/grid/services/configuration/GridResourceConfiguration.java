package org.drools.grid.services.configuration;

import java.io.Serializable;

public abstract class GridResourceConfiguration
    implements
    Serializable {

    private String          name;
    private GenericProvider provider;

    public GridResourceConfiguration() {
    }

    public GridResourceConfiguration(String name,
                                     GenericProvider provider) {
        this.name = name;
        this.provider = provider;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProvider(GenericProvider provider) {
        this.provider = provider;
    }

    public GenericProvider getProvider() {
        return this.provider;
    }
}
