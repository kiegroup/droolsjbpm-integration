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
@XmlRootElement(name = "dmn-unarytests-info")
@XStreamAlias("dmn-unarytests-info")
public class DMNUnaryTestsInfo {

    @XmlElement(name = "unarytests-text")
    @XStreamAlias("unarytests-text")
    private String text;

    @XmlElement(name = "unarytests-expressionLanguage")
    @XStreamAlias("unarytests-expressionLanguage")
    private String expressionLanguage;

    public DMNUnaryTestsInfo() {
        // To avoid the need for kie-server-api to depend on kie-dmn-backend, in order to access DMN's Definitions and DMN's inputdata element
        // build this as DTO and only on server-side leverage setters to populate data as needed.
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getExpressionLanguage() {
        return expressionLanguage;
    }

    public void setExpressionLanguage(String expressionLanguage) {
        this.expressionLanguage = expressionLanguage;
    }
}
