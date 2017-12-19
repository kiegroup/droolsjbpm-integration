/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.controller.websocket.common;

import java.util.HashSet;
import java.util.Set;

import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.Wrapped;
import org.kie.server.controller.api.commands.KieServerControllerDescriptorCommand;
import org.kie.server.controller.api.model.*;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.runtime.ContainerKey;
import org.kie.server.controller.api.model.runtime.ServerInstance;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.*;

public class WebSocketUtils {

    private static Marshaller jsonMarshaller = MarshallerFactory.getMarshaller(null, MarshallingFormat.JSON, WebSocketUtils.class.getClassLoader());
    private static Marshaller jaxbMarshaller = MarshallerFactory.getMarshaller(getModelClasses(), MarshallingFormat.JAXB, WebSocketUtils.class.getClassLoader());

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

        modelClasses.add(KieServerControllerServiceResponse.class);
        modelClasses.add(KieServerControllerDescriptorCommand.class);

        return modelClasses;
    }

    @SuppressWarnings("unchecked")
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
            return (T) ((Wrapped<?>) instance).unwrap();
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
    
    public static MarshallingFormat getFormat(String descriptor) {
        MarshallingFormat format = MarshallingFormat.fromType(descriptor);
        if (format == null) {
            format = MarshallingFormat.valueOf(descriptor);
        }

        return format;
    }
}
