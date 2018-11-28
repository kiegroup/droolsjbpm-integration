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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.kie.dmn.api.core.DMNMessage;
import org.kie.dmn.api.core.DMNMessageType;
import org.kie.dmn.api.feel.runtime.events.FEELEvent;
import org.kie.internal.builder.InternalMessage;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dmn-message")
@XStreamAlias("dmn-message")
//@XmlType(namespace="org.kie.server.api.model.dmn", name = "DMNMessageKS")
public class DMNMessageKS implements DMNMessage {
    
    // This is needed as the DMN's Severity would clash with the Kie server API Severity for marshalling scope
    public static enum DMNMessageSeverityKS {
        /**
         * @deprecated use {@link #INFO} level.
         */
        @Deprecated
        TRACE,
        INFO,
        WARN,
        ERROR;
        
        public static DMNMessageSeverityKS of(Severity value) {
            switch ( value ) {
                case ERROR:
                    return DMNMessageSeverityKS.ERROR;
                case INFO:
                    return DMNMessageSeverityKS.INFO;
                case TRACE:
                    return DMNMessageSeverityKS.TRACE;
                case WARN:
                    return DMNMessageSeverityKS.WARN;
                default:
                    return DMNMessageSeverityKS.ERROR;
            }
        }
        
        public Severity asSeverity() {
            switch ( this ) {
                case ERROR:
                    return Severity.ERROR;
                case INFO:
                    return Severity.INFO;
                case TRACE:
                    return Severity.TRACE;
                case WARN:
                    return Severity.WARN;
                default:
                    return Severity.ERROR;
            }
        }
    }

    @XmlElement(name="dmn-message-severity")
    @XStreamAlias("dmn-message-severity")
    private DMNMessageSeverityKS  severity;
    
    @XmlElement(name="message")
    @XStreamAlias("message")
    private String    message;

    @XmlElement(name="message-type")
    @XStreamAlias("message-type")
    private DMNMessageType messageType;
    
    @XmlElement(name="source-id")
    @XStreamAlias("source-id")
    private String    sourceId;

    public DMNMessageKS() {
        // no-arg constructor for marshalling
    }
    
    public static DMNMessageKS of(DMNMessage value) {
        DMNMessageKS res = new DMNMessageKS();
        res.severity = DMNMessageSeverityKS.of( value.getSeverity() );
        res.message = value.getMessage();
        res.messageType = value.getMessageType();
        res.sourceId = value.getSourceId();
        return res;
    }

    @Override
    public Severity getSeverity() {
        return severity.asSeverity();
    }

    @Override
    public String getMessage() {
        return message;
    }
    
    @Override
    public DMNMessageType getMessageType() {
        return this.messageType;
    }

    @Override
    public String getSourceId() {
        return sourceId;
    }

    @Override
    public Throwable getException() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FEELEvent getFeelEvent() {
        throw new UnsupportedOperationException();
    }

    public Object getSourceReference() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getKieBaseName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InternalMessage setKieBaseName(String kieBaseName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLine() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumn() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Level getLevel() {
        switch (severity) {
            case ERROR:
                return Level.ERROR;
            case INFO:
                return Level.INFO;
            case TRACE:
                return Level.INFO;
            case WARN:
                return Level.WARNING;
            default:
                return Level.ERROR;
        }
    }

    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder();
        sb.append("DMN: ");
        sb.append(this.message);
        sb.append(" (");
        if (this.getPath() != null) {
            sb.append("resource: ");
            sb.append(this.getPath());
            sb.append(", ");
        }
        if (this.getSourceId() != null) { // if toString() via org.kie.api.builder.Message would be helpful to report this 
            sb.append("DMN id: ");
            sb.append(this.getSourceId());
            sb.append(", ");
        }
        sb.append(this.getMessageType().getDescription());
        sb.append(") ");
        return sb.toString();
    }
}
