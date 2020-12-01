/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.data;

import java.io.Serializable;

import org.kie.api.remote.Remotable;

@Remotable
public class Expense implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String concept;
    
    private Money money;

    public Expense() {
    }

    public Money getMoney() {
        return money;
    }
    
    public String getConcept() {
        return concept;
    }
    
    public Expense(Money money, String concept) {
        this.money = money;
        this.concept = concept;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((concept == null) ? 0 : concept.hashCode());
        result = prime * result + ((money == null) ? 0 : money.hashCode());
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
        Expense other = (Expense) obj;
        if (concept == null) {
            if (other.concept != null)
                return false;
        } else if (!concept.equals(other.concept))
            return false;
        if (money == null) {
            if (other.money != null)
                return false;
        } else if (!money.equals(other.money))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "Expense [concept=" + concept + ", money=" + money + "]";
    }

}