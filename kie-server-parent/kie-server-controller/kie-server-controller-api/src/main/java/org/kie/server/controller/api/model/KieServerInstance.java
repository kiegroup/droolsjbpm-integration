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

package org.kie.server.controller.api.model;

import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement(name = "kie-server-instance")
public class KieServerInstance {


    private String identifier;
    private String name;
    private String version;
    private Set<KieServerInstanceInfo> managedInstances;
    private KieServerStatus status;
    private KieServerSetup kieServerSetup;

    /**
     * Returns unique identifier of the KieServer
     * @return
     */
    @XmlElement(name = "server-id")
    public String getIdentifier(){
        return this.identifier;
    }

    /**
     * Returns list of URLs where given KieServer can be found
     * @return
     */
    @XmlElementWrapper(name = "server-managed-instances")
    public Set<KieServerInstanceInfo> getManagedInstances() {
        return this.managedInstances;
    }

    @XmlElement(name = "server-status")
    public KieServerStatus getStatus() {
        if (managedInstances != null && !managedInstances.isEmpty()) {
            int statusBalance = 0;
            for (KieServerInstanceInfo instanceInfo : managedInstances) {
                if (KieServerStatus.UP.equals(instanceInfo.getStatus())) {
                    statusBalance++;
                } else if (KieServerStatus.DOWN.equals(instanceInfo.getStatus())) {
                    statusBalance--;
                }
            }
            if (statusBalance == managedInstances.size()) {
                // all instances are up
                this.status = KieServerStatus.UP;
            } else if ((statusBalance * (-1)) == managedInstances.size()) {
                // all instances are down
                this.status = KieServerStatus.DOWN;
            } else {
                // some instances are up
                this.status = KieServerStatus.PARTIAL_UP;
            }

        } else {
            this.status = KieServerStatus.UNKNOWN;
        }

        return this.status;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setManagedInstances(Set<KieServerInstanceInfo> managedInstances) {
        this.managedInstances = managedInstances;
    }

    public void setStatus(KieServerStatus status) {
        this.status = status;
    }

    @XmlElement(name = "server-setup")
    public KieServerSetup getKieServerSetup() {
        return kieServerSetup;
    }

    public void setKieServerSetup(KieServerSetup kieServerSetup) {
        this.kieServerSetup = kieServerSetup;
    }

    @XmlElement(name = "server-name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "server-version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
