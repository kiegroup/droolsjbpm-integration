/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.commands.optaplanner;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.instance.SolverInstance;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "update-solver-state")
@XStreamAlias( "update-solver-state" )
@XmlAccessorType(XmlAccessType.NONE)
public class UpdateSolverStateCommand
        implements KieServerCommand {

    private static final long serialVersionUID = -1803374525440238478L;

    @XmlAttribute(name = "container-id")
    @XStreamAlias("container-id")
    private String containerId;

    @XmlAttribute(name = "solver-id")
    @XStreamAlias("solver-id")
    private String solverId;

    @XmlElement
    @XStreamAlias("solver-instance")
    private String instance;

    public UpdateSolverStateCommand() {
        super();
    }

    public UpdateSolverStateCommand(String containerId, String solverId, String instance) {
        this.containerId = containerId;
        this.solverId = solverId;
        this.instance = instance;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getSolverId() {
        return solverId;
    }

    public void setSolverId(String solverId) {
        this.solverId = solverId;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof UpdateSolverStateCommand) ) return false;

        UpdateSolverStateCommand that = (UpdateSolverStateCommand) o;

        if ( containerId != null ? !containerId.equals( that.containerId ) : that.containerId != null ) return false;
        if ( solverId != null ? !solverId.equals( that.solverId ) : that.solverId != null ) return false;
        return !(instance != null ? !instance.equals( that.instance ) : that.instance != null);

    }

    @Override
    public int hashCode() {
        int result = containerId != null ? containerId.hashCode() : 0;
        result = 31 * result + (solverId != null ? solverId.hashCode() : 0);
        result = 31 * result + (instance != null ? instance.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UpdateSolverStateCommand{" +
               "containerId='" + containerId + '\'' +
               ", solverId='" + solverId + '\'' +
               ", instance='" + instance + '\'' +
               '}';
    }
}
