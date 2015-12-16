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

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.instance.RequestInfoInstance;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "kie-server-instance-list")
public class KieServerInstanceList {

    @XmlElement(name="kie-server-instance")
    private KieServerInstance[] KieServerInstances;

    public KieServerInstanceList() {
    }

    public KieServerInstanceList(KieServerInstance[] KieServerInstances) {
        this.KieServerInstances = KieServerInstances;
    }

    public KieServerInstanceList(List<KieServerInstance> KieServerInstances) {
        this.KieServerInstances = KieServerInstances.toArray(new KieServerInstance[KieServerInstances.size()]);
    }

    public KieServerInstance[] getKieServerInstances() {
        return KieServerInstances;
    }

    public void setKieServerInstances(KieServerInstance[] KieServerInstances) {
        this.KieServerInstances = KieServerInstances;
    }
}
