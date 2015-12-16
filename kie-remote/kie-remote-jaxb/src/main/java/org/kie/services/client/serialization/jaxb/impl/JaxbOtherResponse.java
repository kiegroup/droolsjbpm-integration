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

package org.kie.services.client.serialization.jaxb.impl;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.api.command.Command;

@XmlRootElement(name = "other-response")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbOtherResponse extends AbstractJaxbCommandResponse<Object> {

    @XmlElement
    private Object result;

    public JaxbOtherResponse() {
    }

    public JaxbOtherResponse(Object result, int i, Command<?> cmd) {
        super(i, cmd);
        this.result = result;
    }

    public JaxbOtherResponse(Map<String, String> variables, String requestUrl) {
        this.url = requestUrl;
        this.status = JaxbRequestStatus.SUCCESS;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

}
