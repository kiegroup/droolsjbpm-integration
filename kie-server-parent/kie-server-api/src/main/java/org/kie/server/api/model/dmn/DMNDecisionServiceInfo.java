/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model.dmn;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dmn-decision-service-info")
@XStreamAlias("dmn-decision-service-info")
public class DMNDecisionServiceInfo {

    @XmlElement(name = "decision-service-id")
    @XStreamAlias("decision-service-id")
    private String id;

    @XmlElement(name = "decision-service-name")
    @XStreamAlias("decision-service-name")
    private String name;

    public DMNDecisionServiceInfo() {
        // To avoid the need for kie-server-api to depend on kie-dmn-backend, in order to access DMN's Definitions and DMN's Decision element
        // build this as DTO and only on server-side leverage setters to populate data as needed.
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
