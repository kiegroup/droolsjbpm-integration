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
public class Pojo1 {

    private String desc;
    private Pojo2 pojo2;

    public Pojo1() {

    }

    public Pojo1(String desc, Pojo2 pojo2) {
        this.desc = desc;
        this.pojo2 = pojo2;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Pojo2 getPojo2() {
        return pojo2;
    }

    public void setPojo2(Pojo2 pojo2) {
        this.pojo2 = pojo2;
    }

    @Override
    public String toString() {
        return "Pojo1{" +
                "desc='" + desc + '\'' +
                ", pojo2=" + pojo2 +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pojo1)) {
            return false;
        }

        Pojo1 pojo1 = (Pojo1) o;

        if (desc != null ? !desc.equals(pojo1.desc) : pojo1.desc != null) {
            return false;
        }
        if (pojo2 != null ? !pojo2.equals(pojo1.pojo2) : pojo1.pojo2 != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = desc != null ? desc.hashCode() : 0;
        result = 31 * result + (pojo2 != null ? pojo2.hashCode() : 0);
        return result;
    }
}
