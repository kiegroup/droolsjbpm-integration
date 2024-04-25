/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.api.marshalling.test.model.dummy.package17;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@SuppressWarnings("java:S1068")

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DummyData87")
public class DummyData87 implements Serializable {

    private static final long serialVersionUID = 5174127032991892033L;

    @XmlAttribute(name = "dummyString")
    private String dummyString;
    private int dummyInt;
    @XmlElement(required = true)
    private Object dummyObj;

    public DummyData87() {
    }

    public DummyData87(int dummyInt, Object dummyObj) {
        this.dummyInt = dummyInt;
        this.dummyObj = dummyObj;
    }

    public DummyData87(int dummyInt, String dummyString, Object dummyObj) {
        this.dummyInt = dummyInt;
        this.dummyString = dummyString;
        this.dummyObj = dummyObj;
    }
}
