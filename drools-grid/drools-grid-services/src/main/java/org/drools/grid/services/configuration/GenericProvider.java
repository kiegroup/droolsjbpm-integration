package org.drools.grid.services.configuration;

import java.io.Serializable;



/**
 * @author salaboy
 */
public abstract class GenericProvider implements Serializable{

	public abstract ProviderType getProviderType();

	public abstract String getId();
}
