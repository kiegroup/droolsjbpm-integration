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

package org.kie.server.api.model.instance;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "request-info-instance-list")
public class RequestInfoInstanceList {

    @XmlElement(name="request-info-instance")
    private RequestInfoInstance[] requestInfoInstances;

    public RequestInfoInstanceList() {
    }

    public RequestInfoInstanceList(RequestInfoInstance[] requestInfoInstances) {
        this.requestInfoInstances = requestInfoInstances;
    }

    public RequestInfoInstanceList(List<RequestInfoInstance> requestInfoInstances) {
        this.requestInfoInstances = requestInfoInstances.toArray(new RequestInfoInstance[requestInfoInstances.size()]);
    }

    public RequestInfoInstance[] getRequestInfoInstances() {
        return requestInfoInstances;
    }

    public void setRequestInfoInstances(RequestInfoInstance[] requestInfoInstances) {
        this.requestInfoInstances = requestInfoInstances;
    }
}
