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

package org.kie.server.api.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.kie.server.api.model.KieServerCommand;

@XmlRootElement(name = "descriptor-command")
@XStreamAlias( "descriptor-command" )
@XmlAccessorType(XmlAccessType.NONE)
public class DescriptorCommand implements KieServerCommand {

    private static final long   serialVersionUID = -1803374525440238418L;

    @XmlElement(name = "service")
    @XStreamAlias( "service" )
    private String service;

    @XmlElement(name = "method")
    @XStreamAlias( "method" )
    private String method;

    @XmlElement(name = "arguments")
    @XStreamAlias( "arguments" )
    private List<Object> arguments;

    @XmlElement(name = "payload")
    private String payload;

    @XmlElement(name = "marshaller-format")
    private String marshallerFormat;

    public DescriptorCommand() {
    }

    public DescriptorCommand(String service, String method, Object... arguments) {
        this.service = service;
        this.method = method;
        this.arguments = Arrays.asList(arguments);
    }

    public DescriptorCommand(String service, String method, String marshallerFormat, Object... arguments) {
        this.service = service;
        this.method = method;
        this.marshallerFormat = marshallerFormat;
        this.arguments = Arrays.asList(arguments);
    }

    public DescriptorCommand(String service, String method, String payload, String marshallerFormat, Object... arguments) {
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
        return "DescriptorCommand{" +
                "service='" + service + '\'' +
                ", method='" + method + '\'' +
                ", arguments='" + arguments + '\'' +
                ", marshallerFormat='" + marshallerFormat + '\'' +
                ", payload=" + payload +
                '}';
    }
}
