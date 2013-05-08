package org.kie.services.client.api.same;

import org.kie.services.client.api.ApiRequestFactoryProvider;
import org.kie.services.client.message.serialization.MessageSerializationProvider.Type;
import org.junit.Assert;

public class SameApiBaseTest extends Assert {

    protected static SameApiRequestHandler getSameApiRequestFactory() {
        SameApiRequestHandler factory = ApiRequestFactoryProvider.createNewSameApiInstance();
        factory.setSerialization(Type.MAP_MESSAGE);

        return factory;
    }
}
