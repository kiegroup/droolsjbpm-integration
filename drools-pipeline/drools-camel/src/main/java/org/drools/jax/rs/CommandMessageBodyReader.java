package org.drools.jax.rs;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.drools.command.Command;
import org.drools.core.util.StringUtils;

@Consumes("text/plain")
@Provider
public class CommandMessageBodyReader implements MessageBodyReader<Object> {

    public boolean isReadable(Class type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return Command.class.isAssignableFrom(type);
    }

    public Object readFrom(Class type,
                           Type genericType,
                           Annotation[] annotations,
                           MediaType mediaType,
                           MultivaluedMap httpHeaders,
                           InputStream entityStream) throws IOException,
                                                    WebApplicationException {
        return StringUtils.toString( entityStream );
    }

}
