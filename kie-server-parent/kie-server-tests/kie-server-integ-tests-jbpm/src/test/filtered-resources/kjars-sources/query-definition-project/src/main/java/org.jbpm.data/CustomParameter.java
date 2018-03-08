/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.data;

import java.lang.Long;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "customparameter-object")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomParameter implements java.io.Serializable {
    private String paramName;
    private long paramValue;

    public CustomParameter() {

    }

    public CustomParameter(String paramName, Long paramValue) {
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    public String getParamName() {
        return paramName;
    }

    public long getParamValue() {
        return paramValue;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public void setParamValue(long paramValue) {
        this.paramValue = paramValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((paramName == null) ? 0 : paramName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CustomParameter other = (CustomParameter) obj;
        if (paramName == null) {
            if (other.paramName != null)
                return false;
        } else if (!paramName.equals(other.paramName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return Long.toString(getParamValue());
    }
}
