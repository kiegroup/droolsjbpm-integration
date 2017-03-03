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

package org.kie.server.api.model.dmn;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.drools.core.xml.jaxb.util.JaxbUnknownAdapter;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.DMNMessage;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dmn-decision-result")
public class DMNDecisionResultKS implements DMNDecisionResult {
    
    @XmlElement(name="decision-id")
    private String           decisionId;
    
    @XmlElement(name="decision-name")
    private String           decisionName;
    
    @XmlElement(name="result")
    @XmlJavaTypeAdapter(JaxbUnknownAdapter.class)
    private Object           result;
    
    @XmlElementWrapper(name="messages")
    private List<DMNMessageKS> messages;

    @XmlElement(name="status")
    private DecisionEvaluationStatus status;

    public DMNDecisionResultKS() {
        // no-arg constructor for marshalling
    }

    public static DMNDecisionResultKS of(DMNDecisionResult value) {
        DMNDecisionResultKS res = new DMNDecisionResultKS();
        res.decisionId = value.getDecisionId();
        res.decisionName = value.getDecisionName();
        res.setResult(value.getResult());
        res.setMessages(value.getMessages());
        res.status = value.getEvaluationStatus();
        return res;
    }

    @Override
    public String getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(String decisionId) {
        this.decisionId = decisionId;
    }

    @Override
    public String getDecisionName() {
        return decisionName;
    }

    public void setDecisionName(String decisionName) {
        this.decisionName = decisionName;
    }

    @Override
    public DecisionEvaluationStatus getEvaluationStatus() {
        return status;
    }

    public void setEvaluationStatus(DecisionEvaluationStatus status) {
        this.status = status;
    }

    @Override
    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = DMNResultKS.stubDMNResult(result);
    }

    @Override
    public List<DMNMessage> getMessages() {
        return (List<DMNMessage>)(List<? extends DMNMessage>) messages;
    }

    public void setMessages(List<DMNMessage> messages) {
        // wrap for serialization:
        for ( DMNMessage m : messages ) {
            this.messages.add(DMNMessageKS.of(m));
        }
    }

    @Override
    public boolean hasErrors() {
        return messages != null && messages.stream().anyMatch( m -> m.getSeverity() == DMNMessage.Severity.ERROR );
    }

}
