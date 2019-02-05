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

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Pojo2 {

    private String desc2;
    private boolean primitiveBoolean;
    private Pojo3 pojo3;

    public Pojo2() {

    }

    public Pojo2(String desc2, boolean primitiveBoolean, Pojo3 pojo3) {
        this.desc2 = desc2;
        this.primitiveBoolean = primitiveBoolean;
        this.pojo3 = pojo3;
    }

    public String getDesc2() {
        return desc2;
    }

    public void setDesc2(String desc2) {
        this.desc2 = desc2;
    }

    public boolean isPrimitiveBoolean() {
        return primitiveBoolean;
    }

    public void setPrimitiveBoolean(boolean primitiveBoolean) {
        this.primitiveBoolean = primitiveBoolean;
    }

    public Pojo3 getPojo3() {
        return pojo3;
    }

    public void setPojo3(Pojo3 pojo3) {
        this.pojo3 = pojo3;
    }

    @Override
    public String toString() {
        return "Pojo2{" +
                "desc2='" + desc2 + '\'' +
                ", primitiveBoolean=" + primitiveBoolean +
                ", pojo3=" + pojo3 +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pojo2)) {
            return false;
        }

        Pojo2 pojo2 = (Pojo2) o;

        if (primitiveBoolean != pojo2.primitiveBoolean) {
            return false;
        }
        if (desc2 != null ? !desc2.equals(pojo2.desc2) : pojo2.desc2 != null) {
            return false;
        }
        if (pojo3 != null ? !pojo3.equals(pojo2.pojo3) : pojo2.pojo3 != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = desc2 != null ? desc2.hashCode() : 0;
        result = 31 * result + (primitiveBoolean ? 1 : 0);
        result = 31 * result + (pojo3 != null ? pojo3.hashCode() : 0);
        return result;
    }
}
