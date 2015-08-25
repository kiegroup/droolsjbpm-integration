/*
 * Copyright 2015 JBoss Inc
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

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "kie-server-instance-info")
public class KieServerInstanceInfo {

    @XmlElement(name = "location")
    private String location;
    @XmlElement(name = "status")
    private KieServerStatus status;

    @XmlElementWrapper(name = "capabilities")
    private List<String> capabilities;

    public KieServerInstanceInfo() {
    }

    public KieServerInstanceInfo(String location, KieServerStatus status, List<String> capabilities) {
        this.location = location;
        this.status = status;
        this.capabilities = capabilities;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public KieServerStatus getStatus() {
        return status;
    }

    public void setStatus(KieServerStatus status) {
        this.status = status;
    }


    public List<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities;
    }
}
