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
public class Pojo1Upper {

    private String SSN;
    private Pojo2Upper pojo2upper;

    public Pojo1Upper() {

    }

    public Pojo1Upper(String sSN, Pojo2Upper pojo2upper) {
        super();
        SSN = sSN;
        this.pojo2upper = pojo2upper;
    }

    public String getSSN() {
        return SSN;
    }

    public void setSSN(String sSN) {
        SSN = sSN;
    }

    public Pojo2Upper getPojo2upper() {
        return pojo2upper;
    }

    public void setPojo2upper(Pojo2Upper pojo2upper) {
        this.pojo2upper = pojo2upper;
    }

    @Override
    public String toString() {
        return "Pojo1Upper [SSN=" + SSN + ", pojo2upper=" + pojo2upper + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((SSN == null) ? 0 : SSN.hashCode());
        result = prime * result + ((pojo2upper == null) ? 0 : pojo2upper.hashCode());
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
        Pojo1Upper other = (Pojo1Upper) obj;
        if (SSN == null) {
            if (other.SSN != null) {
                return false;
            }
        } else if (!SSN.equals(other.SSN)) {
            return false;
        }
        if (pojo2upper == null) {
            if (other.pojo2upper != null) {
                return false;
            }
        } else if (!pojo2upper.equals(other.pojo2upper)) {
            return false;
        }
        return true;
    }

}
