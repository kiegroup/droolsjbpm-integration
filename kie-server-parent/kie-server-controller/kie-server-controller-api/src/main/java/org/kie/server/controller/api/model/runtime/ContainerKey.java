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

package org.kie.server.controller.api.model.runtime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "container-key")
public class ContainerKey {

    @XmlElement(name="sever-template-id")
    private String serverTemplateId;
    @XmlElement(name="container-id")
    private String containerSpecId;
    @XmlElement(name="container-name")
    private String containerName;
    @XmlElement(name="url")
    private String url;

    public ContainerKey() {

    }

    public ContainerKey( final String containerSpecId,
            final String containerName,
            final ServerInstanceKey serverInstanceKey ) {
        this.serverTemplateId = serverInstanceKey.getServerTemplateId();
        this.containerSpecId = containerSpecId;
        this.containerName = containerName;
        this.url = serverInstanceKey.getUrl();
    }

    public String getContainerName() {
        return containerName;
    }

    public String getServerTemplateId() {
        return serverTemplateId;
    }

    public String getContainerSpecId() {
        return containerSpecId;
    }

    public String getUrl() {
        return url;
    }

    public void setServerTemplateId(String serverTemplateId) {
        this.serverTemplateId = serverTemplateId;
    }

    public void setContainerSpecId(String containerSpecId) {
        this.containerSpecId = containerSpecId;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "ContainerKey{" +
                "serverTemplateId='" + serverTemplateId + '\'' +
                ", containerSpecId='" + containerSpecId + '\'' +
                ", containerName='" + containerName + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContainerKey that = (ContainerKey) o;

        if (containerName != null ? !containerName.equals(that.containerName) : that.containerName != null) {
            return false;
        }
        if (containerSpecId != null ? !containerSpecId.equals(that.containerSpecId) : that.containerSpecId != null) {
            return false;
        }
        if (serverTemplateId != null ? !serverTemplateId.equals(that.serverTemplateId) : that.serverTemplateId != null) {
            return false;
        }
        if (url != null ? !url.equals(that.url) : that.url != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = serverTemplateId != null ? serverTemplateId.hashCode() : 0;
        result = 31 * result + (containerSpecId != null ? containerSpecId.hashCode() : 0);
        result = 31 * result + (containerName != null ? containerName.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }
}
