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
public class Employee implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int seniority;
    
    private String name;

    public Employee() {
    }

    public int getSeniority() {
        return seniority;
    }
    
    public String getName() {
        return name;
    }
    
    public Employee(int seniority, String name) {
        this.seniority = seniority;
        this.name = name;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + seniority;
        if (name != null) {
            result = 31 * result + name.hashCode();
        }
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Employee))
            return false;
        Employee other = (Employee)o;
        boolean nameEquals = (this.name == null && other.name == null)
          || (this.name != null && this.name.equals(other.name));
        return this.seniority == other.seniority && nameEquals;
    }
    
    @Override
    public String toString() {
        return "Employee [seniority=" + seniority + ", name=" + name + "]";
    }

}