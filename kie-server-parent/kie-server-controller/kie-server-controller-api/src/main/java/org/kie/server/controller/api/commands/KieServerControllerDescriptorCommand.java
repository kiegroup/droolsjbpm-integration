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

package org.kie.server.controller.api.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.*;

@XmlRootElement(name = "controller-descriptor-command")
@XmlAccessorType(XmlAccessType.NONE)
public class KieServerControllerDescriptorCommand implements KieServerCommand {

    private static final long serialVersionUID = -1803374525440238418L;

    @XmlElement(name = "service")
    private String service;

    @XmlElement(name = "method")
    private String method;

    @XmlElements({
            @XmlElement(name = "container-spec-details", type = ContainerSpec.class),
            @XmlElement(name = "server-template-details", type = ServerTemplate.class),
            @XmlElement(name = "rule-config", type = RuleConfig.class),
            @XmlElement(name = "process-config", type = ProcessConfig.class),
            @XmlElement(name = "server-config", type = ServerConfig.class),
            @XmlElement(name = "container-config", type = ContainerConfig.class),
            @XmlElement(name = "container-spec-key", type = ContainerSpecKey.class),
            @XmlElement(name = "server-instance-key", type = ServerInstanceKey.class),
            @XmlElement(name = "release-id", type = ReleaseId.class),
            @XmlElement(name = "capability", type = Capability.class),
            @XmlElement(name = "arguments", type = String.class),
            @XmlElement(name = "long", type = Long.class)
    })
    private List<Object> arguments;

    @XmlElement(name = "payload")
    private String payload;

    @XmlElement(name = "marshaller-format")
    private String marshallerFormat;

    public KieServerControllerDescriptorCommand() {
    }

    public KieServerControllerDescriptorCommand(String service,
                                                String method,
                                                Object... arguments) {
        this(service,
             method,
             null,
             arguments);
    }

    public KieServerControllerDescriptorCommand(String service,
                                                String method,
                                                String marshallerFormat,
                                                Object... arguments) {
        this(service,
             method,
             null,
             marshallerFormat,
             arguments);
    }

    public KieServerControllerDescriptorCommand(String service,
                                                String method,
                                                String payload,
                                                String marshallerFormat,
                                                Object... arguments) {
        this.service = service;
        this.method = method;
        this.payload = payload;
        this.marshallerFormat = marshallerFormat;
        this.arguments = Arrays.asList(arguments);
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<Object> getArguments() {
        if (arguments == null) {
            return new ArrayList<Object>();
        }
        return arguments;
    }

    public void setArguments(List<Object> arguments) {
        this.arguments = arguments;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getMarshallerFormat() {
        return marshallerFormat;
    }

    public void setMarshallerFormat(String marshallerFormat) {
        this.marshallerFormat = marshallerFormat;
    }

    @Override
    public String toString() {
        return "KieServerControllerDescriptorCommand{" +
                "service='" + service + '\'' +
                ", method='" + method + '\'' +
                ", arguments='" + arguments + '\'' +
                ", marshallerFormat='" + marshallerFormat + '\'' +
                ", payload=" + payload +
                '}';
    }
}
