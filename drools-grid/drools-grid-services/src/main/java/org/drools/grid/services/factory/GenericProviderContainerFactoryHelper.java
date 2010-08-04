package org.drools.grid.services.factory;

import org.drools.grid.services.configuration.GenericProvider;
import org.drools.grid.services.configuration.MinaProvider;
import org.drools.grid.services.configuration.RioProvider;

public class GenericProviderContainerFactoryHelper {

	public static <T> T doOnGenericProvider(GenericProvider provider, GenericProviderContainerBuilder<T> builder){
        switch (provider.getProviderType()){
            case Local: {
            	return builder.onLocalProvider();
            }
            case RemoteMina: {
            	return builder.onMinaProvider((MinaProvider)provider);
            }
            case RemoteHornetQ: {
            	return builder.onHornetQProvider();
            }
            case DistributedRio: {
            	return builder.onRioProvider((RioProvider)provider);
            }
        }
        throw new IllegalArgumentException("Unmatcheable provider " + provider.getProviderType().name());
	}
	
}
