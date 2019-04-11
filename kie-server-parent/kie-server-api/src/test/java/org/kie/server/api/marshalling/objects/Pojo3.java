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

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Pojo3 {

    private String desc3;

    public Pojo3() {
    }

    public Pojo3(String desc3) {
        this.desc3 = desc3;
    }

    public String getDesc3() {
        return desc3;
    }

    public void setDesc3(String desc3) {
        this.desc3 = desc3;
    }

    @Override
    public String toString() {
        return "Pojo3{" +
                "desc3='" + desc3 + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pojo3)) {
            return false;
        }

        Pojo3 pojo3 = (Pojo3) o;

        if (desc3 != null ? !desc3.equals(pojo3.desc3) : pojo3.desc3 != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return desc3 != null ? desc3.hashCode() : 0;
    }
}
