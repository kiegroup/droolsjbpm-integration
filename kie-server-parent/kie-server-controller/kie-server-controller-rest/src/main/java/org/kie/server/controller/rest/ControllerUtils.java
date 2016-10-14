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

import org.kie.server.common.rest.RestEasy960Util;
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
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.runtime.ContainerKey;
import org.kie.server.controller.api.model.runtime.ServerInstance;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ContainerSpecKey;
import org.kie.server.controller.api.model.spec.ContainerSpecList;
import org.kie.server.controller.api.model.spec.ProcessConfig;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerConfig;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.kie.server.controller.api.model.spec.ServerTemplateList;

public class ControllerUtils {

    private static Marshaller jsonMarshaller = MarshallerFactory.getMarshaller(getMinimalModelClasses(), MarshallingFormat.JSON, ControllerUtils.class.getClassLoader());
    private static Marshaller jaxbMarshaller = MarshallerFactory.getMarshaller(getModelClasses(), MarshallingFormat.JAXB, ControllerUtils.class.getClassLoader());

    public static Set<Class<?>> getModelClasses() {
        Set<Class<?>> modelClasses = new HashSet<Class<?>>();

        modelClasses.add(KieServerInstance.class);
        modelClasses.add(KieServerInstanceList.class);
        modelClasses.add(KieServerInstanceInfo.class);
        modelClasses.add(KieServerSetup.class);
        modelClasses.add(KieServerStatus.class);

        modelClasses.add(ServerInstance.class);
        modelClasses.add(ServerInstanceKey.class);
        modelClasses.add(ServerTemplate.class);
        modelClasses.add(ServerTemplateKey.class);
        modelClasses.add(ServerConfig.class);
        modelClasses.add(RuleConfig.class);
        modelClasses.add(ProcessConfig.class);
        modelClasses.add(ContainerSpec.class);
        modelClasses.add(ContainerSpecKey.class);
        modelClasses.add(Container.class);
        modelClasses.add(ContainerKey.class);
        modelClasses.add(ServerTemplateList.class);
        modelClasses.add(ContainerSpecList.class);

        return modelClasses;
    }

    public static Set<Class<?>> getMinimalModelClasses() {
        Set<Class<?>> modelClasses = new HashSet<Class<?>>();

        modelClasses.add(RuleConfig.class);
        modelClasses.add(ProcessConfig.class);

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
            v = Variant.mediaTypes(getMediaType(headers)).add().build().get(0);;
        }
        return v;
    }

    public static String getContentType(HttpHeaders headers) {
        // default to application/xml
        String contentType = MediaType.APPLICATION_XML_TYPE.toString();
        // check variant that is based on accept header important in case of GET as then Content-Type might not be given
        Variant v = RestEasy960Util.getVariant(headers);
        if (v != null) {
            // set the default to selected variant
            contentType = v.getMediaType().toString();
        }
        // now look for actual Content-Type header
        List<String> contentTypeHeader = headers.getRequestHeader(HttpHeaders.CONTENT_TYPE);
        if (contentTypeHeader != null && !contentTypeHeader.isEmpty()) {
            contentType = contentTypeHeader.get(0);

        }
        List<String> kieContentTypeHeader = headers.getRequestHeader(KieServerConstants.KIE_CONTENT_TYPE_HEADER);
        if (kieContentTypeHeader != null && !kieContentTypeHeader.isEmpty()) {
            contentType = kieContentTypeHeader.get(0);
        }

        return contentType;
    }

    public static MediaType getMediaType(HttpHeaders httpHeaders) {
        String contentType = getContentType(httpHeaders);
        try {
            return MediaType.valueOf(contentType);
        } catch (IllegalArgumentException e) {
            MarshallingFormat format = MarshallingFormat.fromType(contentType);
            switch (format) {
                case JAXB:
                    return MediaType.APPLICATION_XML_TYPE;

                case XSTREAM:
                    return MediaType.APPLICATION_XML_TYPE;

                case JSON:
                    return MediaType.APPLICATION_JSON_TYPE;

                default:
                    return MediaType.APPLICATION_XML_TYPE;
            }
        }
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
