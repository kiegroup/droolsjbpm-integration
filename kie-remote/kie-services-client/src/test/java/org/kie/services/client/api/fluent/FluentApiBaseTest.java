package org.kie.services.client.api.fluent;

import org.kie.services.client.api.ApiRequestFactoryProvider;
import org.kie.services.client.message.serialization.MessageSerializationProvider.Type;
import org.junit.Assert;

public class FluentApiBaseTest extends Assert { 

    protected FluentApiRequestHandler getFluentApiRequestFactory() { 
        FluentApiRequestHandler factory = ApiRequestFactoryProvider.createNewFluentApiInstance();
        factory.setSerialization(Type.MAP_MESSAGE);
        return factory;
    }
    
 
}
