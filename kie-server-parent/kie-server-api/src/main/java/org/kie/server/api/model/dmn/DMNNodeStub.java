/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.fasterxml.jackson.annotation.JsonValue;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dmn-node-stub")
@XStreamAlias("dmn-node-stub")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"dmnNode"})
public class DMNNodeStub {

    @XmlValue
    private String dmnNode;

    public DMNNodeStub() {
        // empty constructor for marshalling
    }

    static DMNNodeStub of(Object dmnNode) {
        DMNNodeStub res = new DMNNodeStub();
        res.dmnNode = dmnNode.toString();
        return res;
    }

    @JsonValue
    @Override
    public String toString() {
        return dmnNode;
    }
}
