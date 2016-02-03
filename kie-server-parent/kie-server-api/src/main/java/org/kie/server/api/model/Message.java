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

package org.kie.server.api.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XmlRootElement(name="kie-message")
@XmlType(name = "kie-message")
@XStreamAlias( "kie-message" )
public class Message {

    private Severity severity;
    private Date timestamp;
    private List<String> messages = new ArrayList<String>();

    public Message() {
    }

    public Message(Severity severity, String message) {
        this.severity = severity;
        addMessage(message);
        this.timestamp = new Date();
    }

    public Message(Severity severity, List<String> messages) {
        this.severity = severity;
        this.messages = messages;
        this.timestamp = new Date();
    }

    @XmlElement(name="severity")
    public Severity getSeverity() {
        return severity;
    }

    @XmlElement(name="content")
    public Collection<String> getMessages() {
        return messages;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    @XmlElement(name="timestamp")
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
