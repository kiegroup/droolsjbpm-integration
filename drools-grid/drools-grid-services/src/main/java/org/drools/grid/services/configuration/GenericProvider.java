package org.drools.grid.services.configuration;

import java.io.Serializable;
import org.drools.grid.GenericNodeConnector;



/**
 * @author salaboy
 */

public interface GenericProvider<T> extends Serializable{

	 ProviderType getProviderType();

         GenericNodeConnector getConnector(String connectorString);

	 String getId();

}
