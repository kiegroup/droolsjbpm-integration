/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.camel.container.module;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.WildcardTypePermission;
import org.drools.core.runtime.help.impl.BatchExecutionHelperProviderImpl;

public class XStreamMarshallerProvider {

    public XStream newXStreamMarshaller() {
        XStream xstreamMarshaller = new BatchExecutionHelperProviderImpl().newXStreamMarshaller();
        // Add your domain object classes
        String[] allowList = new String[]{
                                          "org.kie.camel.container.api.model.Person"
        };
        xstreamMarshaller.addPermission( new WildcardTypePermission( allowList ) );
        return xstreamMarshaller;
    }
}
