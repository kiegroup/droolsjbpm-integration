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

package org.kie.server.controller.api.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.KieServiceResponse;
import org.kie.server.controller.api.model.runtime.ContainerList;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.runtime.ServerInstanceKeyList;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ContainerSpecList;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.kie.server.controller.api.model.spec.ServerTemplateKeyList;
import org.kie.server.controller.api.model.spec.ServerTemplateList;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement(name = "controller-response")
@XmlAccessorType(XmlAccessType.NONE)
public class KieServerControllerServiceResponse<T> implements KieServiceResponse<T> {

    @XmlAttribute
    private ResponseType type;

    @XmlAttribute
    private String msg;

    @XmlElements({
            //Kie Server Controller model
            @XmlElement(name = "server-template-list", type = ServerTemplateList.class),
            @XmlElement(name = "server-template-details", type = ServerTemplate.class),
            @XmlElement(name = "server-template-key-list", type = ServerTemplateKeyList.class),
            @XmlElement(name = "server-template-key", type = ServerTemplateKey.class),
            @XmlElement(name = "container-spec-list", type = ContainerSpecList.class),
            @XmlElement(name = "container-spec-details", type = ContainerSpec.class),
            @XmlElement(name = "server-instance-key-list", type = ServerInstanceKeyList.class),
            @XmlElement(name = "server-instance-key", type = ServerInstanceKey.class),
            @XmlElement(name = "container-details-list", type = ContainerList.class)
    })
    @JsonProperty
    private T result;

    public KieServerControllerServiceResponse() {
        super();
    }

    public KieServerControllerServiceResponse(final ResponseType type,
                                              final String msg) {
        this.type = type;
        this.msg = msg;
    }

    public KieServerControllerServiceResponse(final ResponseType type,
                                              final String msg,
                                              final T result) {
        this.type = type;
        this.msg = msg;
        this.result = result;
    }

    @Override
    public ResponseType getType() {
        return type;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public T getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "KieServerControllerServiceResponse{" +
                "type=" + type +
                ", msg='" + msg + '\'' +
                ", result=" + result +
                '}';
    }
}
