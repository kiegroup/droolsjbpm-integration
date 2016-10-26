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

package org.kie.server.api.model.admin;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "email-notification")
public class EmailNotification {

    @XmlElement(name="from")
    private String from;
    @XmlElement(name="reply-to")
    private String replyTo;
    @XmlElement(name="users")
    private List<String> users;
    @XmlElement(name="groups")
    private List<String> groups;
    @XmlElement(name="subject")
    private String subject;
    @XmlElement(name="body")
    private String body;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private EmailNotification emailNotification = new EmailNotification();

        public EmailNotification build() {
            return emailNotification;
        }

        public Builder from(String from) {
            emailNotification.setFrom(from);
            return this;
        }

        public Builder replyTo(String replyTo) {
            emailNotification.setReplyTo(replyTo);
            return this;
        }

        public Builder subject(String subject) {
            emailNotification.setSubject(subject);
            return this;
        }

        public Builder body(String body) {
            emailNotification.setBody(body);
            return this;
        }

        public Builder users(List<String> users) {
            emailNotification.setUsers(users);
            return this;
        }

        public Builder groups(List<String> groups) {
            emailNotification.setGroups(groups);
            return this;
        }
    }

    @Override
    public String toString() {
        return "EmailNotification{" +
                "from='" + from + '\'' +
                ", replyTo='" + replyTo + '\'' +
                ", users=" + users +
                ", groups=" + groups +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
