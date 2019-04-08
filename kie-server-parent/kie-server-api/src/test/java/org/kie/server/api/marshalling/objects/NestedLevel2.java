/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.api.marshalling.objects;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class NestedLevel2 implements Serializable {

    private static final long serialVersionUID = -3792827959595912475L;

    @XmlElement(name = "nestedLevel3-overridenByJaxbAnnotation")
    private NestedLevel3 nestedLevel3;

    public NestedLevel3 getNestedLevel3() {
        return nestedLevel3;
    }

    public void setNestedLevel3(NestedLevel3 nestedLevel3) {
        this.nestedLevel3 = nestedLevel3;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nestedLevel3 == null) ? 0 : nestedLevel3.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NestedLevel2 other = (NestedLevel2) obj;
        if (nestedLevel3 == null) {
            if (other.nestedLevel3 != null) {
                return false;
            }
        } else if (!nestedLevel3.equals(other.nestedLevel3)) {
            return false;
        }
        return true;
    }
}
