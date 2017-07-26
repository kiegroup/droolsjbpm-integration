package org.kie.server.controller.websocket.common.handlers;

import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;

public interface InternalMessageHandler {
    Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JSON, InternalMessageHandler.class.getClassLoader());
    
    default <T> T deserialize(String content, Class<T> type) {
        if (type == null) {
            return null;
        }

        try {
            return marshaller.unmarshall(content, type);
        } catch ( MarshallingException e ) {
            throw new IllegalStateException( "Error while deserializing data received from server!", e );
        }
    }

    default String serialize(Object object) {
        if (object == null) {
            return "";
        }

        try {
            return marshaller.marshall(object);
        } catch ( MarshallingException e ) {
            throw new IllegalStateException( "Error while serializing request data!", e );
        }
    }

    String onMessage(String message);
    
    default InternalMessageHandler getNextHandler() {
        return null;
    }
}
