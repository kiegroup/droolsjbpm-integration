/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerConfigItem;

@XmlRootElement(name = "script")
@XStreamAlias( "script" )
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "script")
public class CommandScript implements Serializable {

    private static final long serialVersionUID = 510l;

    @XmlElements({
                         @XmlElement(name = "register-server-controller", type = RegisterServerControllerCommand.class),
                         @XmlElement(name = "get-server-info", type = GetServerInfoCommand.class),
                         @XmlElement(name = "create-container", type = CreateContainerCommand.class),
                         @XmlElement(name = "get-container-info", type = GetContainerInfoCommand.class),
                         @XmlElement(name = "list-containers", type = ListContainersCommand.class),
                         @XmlElement(name = "dispose-container", type = DisposeContainerCommand.class),
                         @XmlElement(name = "get-scanner-info", type = GetScannerInfoCommand.class),
                         @XmlElement(name = "update-scanner", type = UpdateScannerCommand.class),
                         @XmlElement(name = "update-release-id", type = UpdateReleaseIdCommand.class),
                         @XmlElement(name = "call-container", type = CallContainerCommand.class),
                         @XmlElement(name = "descriptor-command", type = DescriptorCommand.class),

            // TODO can this be added somewhere else? if not here JAXRS cannot deserialize content
            @XmlElement(name = "kie-server-config", type = KieServerConfig.class),
            @XmlElement(name = "kie-server-config-item", type = KieServerConfigItem.class)
                 })
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    protected List<KieServerCommand> commands;

    public CommandScript() {
    }

    public CommandScript(List<KieServerCommand> commands) {
        this.commands = commands;
    }

    public List<KieServerCommand> getCommands() {
        if (commands == null) {
            commands = new ArrayList<KieServerCommand>();
        }
        return this.commands;
    }

    public String toString() {
        return "CommandScriptImpl{ commands=" + commands +
                '}';
    }
}
