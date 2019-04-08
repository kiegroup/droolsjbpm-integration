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

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dmn-model-info-list")
@XStreamAlias("dmn-model-info-list")
public class DMNModelInfoList {

    // note Jackson annotation is needed on this field and getter, in order for Jackson to NOT use JAXB annotation but proper Jackson annotation
    @XmlElement(name = "model")
    @JsonIgnore
    @XStreamImplicit(itemFieldName = "model")
    private DMNModelInfo[] models;

    public DMNModelInfoList() {
    }

    public DMNModelInfoList(List<DMNModelInfo> models) {
        this.models = models.toArray(new DMNModelInfo[]{});
    }

    @JsonProperty("models")
    public List<DMNModelInfo> getModels() {
        return Arrays.asList(models);
    }

    public void setModels(List<DMNModelInfo> models) {
        this.models = models.toArray(new DMNModelInfo[]{});
    }
}
