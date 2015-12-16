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

package org.kie.server.api.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="responses")
@XStreamAlias( "responses" )
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceResponsesList {

    @XmlElement(name = "response")
    @XStreamImplicit(itemFieldName = "response")
    private List<ServiceResponse<? extends Object>> responses;

    public ServiceResponsesList() {
        responses = new ArrayList<ServiceResponse<? extends Object>>();
    }

    public ServiceResponsesList(List<ServiceResponse<? extends Object>> responses) {
        this.responses = responses;
    }

    public List<ServiceResponse<? extends Object>> getResponses() {
        return responses;
    }

    public void setResponses(List<ServiceResponse<? extends Object>> responses) {
        this.responses = responses;
    }
}
