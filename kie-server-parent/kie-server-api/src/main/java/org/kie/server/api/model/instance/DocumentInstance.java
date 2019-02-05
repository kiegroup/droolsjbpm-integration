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

package org.kie.server.api.model.instance;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "document-instance")
public class DocumentInstance {

    @XmlElement(name="document-id")
    private String identifier;

    @XmlElement(name="document-name")
    private String name;

    @XmlElement(name="document-link")
    private String link;

    @XmlElement(name="document-size")
    private long size;

    @XmlElement(name="document-last-mod")
    private Date lastModified;

    @XmlElement(name="document-content")
    private byte[] content;

    public DocumentInstance() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public static class Builder {

        private DocumentInstance documentInstance = new DocumentInstance();

        public DocumentInstance build() {
            return documentInstance;
        }

        public Builder id(String id) {
            documentInstance.setIdentifier(id);
            return this;
        }

        public Builder name(String name) {
            documentInstance.setName(name);
            return this;
        }

        public Builder link(String link) {
            documentInstance.setLink(link);
            return this;
        }

        public Builder size(long size) {
            documentInstance.setSize(size);
            return this;
        }

        public Builder lastModified(Date lastModified) {
            documentInstance.setLastModified(lastModified);
            return this;
        }

        public Builder content(byte[] content) {
            documentInstance.setContent(content);
            return this;
        }
    }

    @Override
    public String toString() {
        return "DocumentInstance{" +
                "identifier='" + identifier + '\'' +
                ", name='" + name + '\'' +
                ", link='" + link + '\'' +
                ", size=" + size +
                ", lastModified=" + lastModified +
                '}';
    }
}
