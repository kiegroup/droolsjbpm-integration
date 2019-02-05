/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.marshalling.objects;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "nestedLevel2"
})
@XmlRootElement(name = "NestedLevel1")
public class NestedLevel1 implements Serializable {

    private static final long serialVersionUID = -6019325375785439649L;

    @XmlElement(name = "nestedLevel2-overridenByJaxbAnnotation")
    protected NestedLevel2 nestedLevel2;

    public NestedLevel2 getNestedLevel2() {
        return nestedLevel2;
    }

    public void setNestedLevel2(NestedLevel2 nestedLevel2) {
        this.nestedLevel2 = nestedLevel2;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nestedLevel2 == null) ? 0 : nestedLevel2.hashCode());
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
        NestedLevel1 other = (NestedLevel1) obj;
        if (nestedLevel2 == null) {
            if (other.nestedLevel2 != null) {
                return false;
            }
        } else if (!nestedLevel2.equals(other.nestedLevel2)) {
            return false;
        }
        return true;
    }
}
