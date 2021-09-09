/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
public class Pojo2Upper {

    private boolean USCitizen;

    public Pojo2Upper() {

    }

    public Pojo2Upper(boolean uSCitizen) {
        super();
        USCitizen = uSCitizen;
    }

    public boolean isUSCitizen() {
        return USCitizen;
    }

    public void setUSCitizen(boolean uSCitizen) {
        USCitizen = uSCitizen;
    }

    @Override
    public String toString() {
        return "Pojo2Upper [USCitizen=" + USCitizen + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (USCitizen ? 1231 : 1237);
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
        Pojo2Upper other = (Pojo2Upper) obj;
        if (USCitizen != other.USCitizen) {
            return false;
        }
        return true;
    }

}
