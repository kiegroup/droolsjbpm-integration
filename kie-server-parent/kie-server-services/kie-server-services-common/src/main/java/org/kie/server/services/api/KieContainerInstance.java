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

package org.kie.server.services.api;

import java.util.Set;

import org.kie.api.builder.KieScanner;
import org.kie.api.runtime.KieContainer;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;

public interface KieContainerInstance {

    String getContainerId();

    KieContainer getKieContainer();

    KieContainerStatus getStatus();

    KieContainerResource getResource();

    KieScanner getScanner();

    Marshaller getMarshaller(MarshallingFormat format);

    void disposeMarshallers();

    void addService(Object service);

    boolean addExtraClasses(Set<Class<?>> extraClassList);

    void clearExtraClasses();

    Set<Class<?>> getExtraClasses();

    <T> T getService(Class<T> serviceType);

    <T> T removeService(Class<T> serviceType);
}
