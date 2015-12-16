/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.controller.rest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.kie.remote.common.rest.RestEasy960Util;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.Wrapped;
import org.kie.server.controller.api.model.KieServerInstance;
import org.kie.server.controller.api.model.KieServerInstanceInfo;
import org.kie.server.controller.api.model.KieServerInstanceList;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.controller.api.model.KieServerStatus;

public class ControllerUtils {

    public static Variant defaultVariant = Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE).add().build().get(0);

    private static Marshaller jsonMarshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JSON, ControllerUtils.class.getClassLoader());
    private static Marshaller jaxbMarshaller = MarshallerFactory.getMarshaller(getModelClasses(), MarshallingFormat.JAXB, ControllerUtils.class.getClassLoader());

    public static Set<Class<?>> getModelClasses() {
        Set<Class<?>> modelClasses = new HashSet<Class<?>>();

        modelClasses.add(KieServerInstance.class);
        modelClasses.add(KieServerInstanceList.class);
        modelClasses.add(KieServerInstanceInfo.class);
        modelClasses.add(KieServerSetup.class);
        modelClasses.add(KieServerStatus.class);

        return modelClasses;
    }

    public static Response createCorrectVariant(String responseObj, HttpHeaders headers, javax.ws.rs.core.Response.Status status) {
        Response.ResponseBuilder responseBuilder = null;
        Variant v = getVariant(headers);

        if( status != null ) {
            responseBuilder = Response.status(status).entity(responseObj).variant(v);
        } else {
            responseBuilder = Response.ok(responseObj, v);
        }
        return responseBuilder.build();
    }


    public static Variant getVariant(HttpHeaders headers) {
        Variant v = RestEasy960Util.getVariant(headers);
        if( v == null ) {
            v = defaultVariant;
        }
        return v;
    }

    public static String getContentType(HttpHeaders headers) {
        Variant v = getVariant(headers);
        String contentType = v.getMediaType().getSubtype();

        List<String> kieContentTypeHeader = headers.getRequestHeader(KieServerConstants.KIE_CONTENT_TYPE_HEADER);
        if (kieContentTypeHeader != null && !kieContentTypeHeader.isEmpty()) {
            contentType = kieContentTypeHeader.get(0);
        }

        return contentType;
    }

    public static MarshallingFormat getFormat(String descriptor) {
        MarshallingFormat format = MarshallingFormat.fromType(descriptor);
        if (format == null) {
            format = MarshallingFormat.valueOf(descriptor);
        }

        return format;
    }

    public static <T> T unmarshal(String data, String marshallingFormat, Class<T> unmarshalType) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        MarshallingFormat format = getFormat(marshallingFormat);

        Marshaller marshaller = null;
        switch (format) {
            case JAXB: {
                marshaller = jaxbMarshaller;
                break;
            }
            case JSON: {
                marshaller = jsonMarshaller;
                break;
            }
            default: {
                marshaller = jsonMarshaller;
                break;
            }
        }

        Object instance = marshaller.unmarshall(data, unmarshalType);

        if (instance instanceof Wrapped) {
            return (T) ((Wrapped) instance).unwrap();
        }

        return (T) instance;
    }

    public static String marshal(String marshallingFormat, Object entity) {
        MarshallingFormat format = getFormat(marshallingFormat);


        if (format == null) {
            throw new IllegalArgumentException("Unknown marshalling format " + marshallingFormat);
        }

        Marshaller marshaller = null;
        switch (format) {
            case JAXB: {
                marshaller = jaxbMarshaller;
                break;
            }
            case JSON: {
                marshaller = jsonMarshaller;
                break;
            }
            default: {
                marshaller = jsonMarshaller;
                break;
            }
        }

        return marshaller.marshall(entity);

    }

    public static String getUser() {
        return System.getProperty(KieServerConstants.CFG_KIE_USER, "kieserver");
    }

    public static String getPassword() {
        return System.getProperty(KieServerConstants.CFG_KIE_PASSWORD, "kieserver1!");
    }
}
