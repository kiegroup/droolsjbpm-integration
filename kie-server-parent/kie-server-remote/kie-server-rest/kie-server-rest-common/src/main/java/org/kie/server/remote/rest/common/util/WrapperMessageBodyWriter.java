package org.kie.server.remote.rest.common.util;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.remote.rest.common.util.RestUtils.Wrapper;

@Provider
public class WrapperMessageBodyWriter implements MessageBodyWriter<Wrapper<?>> {

    private static final boolean STRICT_ID_FORMAT = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_SERVER_STRICT_ID_FORMAT, "false"));

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == Wrapper.class;
    }

    @Override
    public void writeTo(Wrapper<?> identifier,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {

        Map<String, String> parameters = mediaType.getParameters();
        Boolean isStrict = Boolean.parseBoolean(parameters.get("strict"));

        MarshallingFormat format = MarshallingFormat.fromType(mediaType.toString());
        Marshaller marshaller = MarshallerFactory.getMarshaller(format, Thread.currentThread().getContextClassLoader());
        boolean isJsonFormat = MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType);
        boolean isXmlFormat = MediaType.APPLICATION_XML_TYPE.isCompatible(mediaType);
        if ((isStrict || STRICT_ID_FORMAT) && (isJsonFormat || isXmlFormat)) {
            entityStream.write(marshaller.marshall(identifier).getBytes());
        } else {
            entityStream.write(identifier.getId().toString().getBytes());
        }

    }

}
