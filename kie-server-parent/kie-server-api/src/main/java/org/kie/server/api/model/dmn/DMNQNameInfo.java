/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model.dmn;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dmn-qname-info")
@XStreamAlias("dmn-qname-info")
public class DMNQNameInfo {

    @XmlElement(name = "namespace-uri")
    @XStreamAlias("namespace-uri")
    private String namespaceURI;

    @XmlElement(name = "local-part")
    @XStreamAlias("local-part")
    private String localPart;

    @XmlElement(name = "prefix")
    @XStreamAlias("prefix")
    private String prefix;

    public DMNQNameInfo() {
        // empty constructor.
    }

    public static DMNQNameInfo of(QName from) {
        DMNQNameInfo result = new DMNQNameInfo();
        result.namespaceURI = from.getNamespaceURI();
        result.localPart = from.getLocalPart();
        result.prefix = from.getPrefix();
        return result;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public String getLocalPart() {
        return localPart;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setNamespaceURI(String namespaceURI) {
        this.namespaceURI = namespaceURI;
    }

    public void setLocalPart(String localPart) {
        this.localPart = localPart;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
