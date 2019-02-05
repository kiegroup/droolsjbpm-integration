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

package org.kie.server.api.model.cases;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "case-comment")
public class CaseComment {

    @XmlElement(name="id")
    private String id;
    @XmlElement(name="author")
    private String author;
    @XmlElement(name="text")
    private String text;
    @XmlElement(name="added-at")
    private Date addedAt;
    @XmlElement(name="restricted-to")
    private String[] restrictedTo;
    

    public CaseComment() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Date addedAt) {
        this.addedAt = addedAt;
    }
    
    public String[] getRestrictedTo() {
        return restrictedTo;
    }
    
    public void setRestrictedTo(String[] restrictedTo) {
        this.restrictedTo = restrictedTo;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private CaseComment adHocFragment = new CaseComment();

        public CaseComment build() {
            return adHocFragment;
        }

        public Builder id(String id) {
            adHocFragment.setId(id);
            return this;
        }

        public Builder author(String author) {
            adHocFragment.setAuthor(author);
            return this;
        }

        public Builder text(String text) {
            adHocFragment.setText(text);
            return this;
        }

        public Builder addedAt(Date addedAt) {
            adHocFragment.setAddedAt(addedAt);
            return this;
        }
        
        public Builder restrictedTo(String[] restrictedTo) {
            adHocFragment.setRestrictedTo(restrictedTo);
            return this;
        }
    }

    @Override
    public String toString() {
        return "CaseComment{" +
                "author='" + author + '\'' +
                ", text='" + text + '\'' +
                ", addedAt=" + addedAt +
                '}';
    }
}
