/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.runtime.pipeline.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample Department bean to demostrate main excel export features
 * author: Leonid Vysochyn
 */
public class Company {
    private String name;
    private Employee chief = new Employee();
    private List employee = new ArrayList();

    public Company() {
    }

    public Company(String name) {
        this.name = name;
    }

    public Company(String name, Employee chief, List staff) {
        this.name = name;
        this.chief = chief;
        this.employee = staff;
    }

    public void addEmployee(Employee employee) {
        this.employee.add(employee);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Employee getChief() {
        return chief;
    }

    public void setChief(Employee chief) {
        this.chief = chief;
    }    

    public List getEmployee() {
        return employee;
    }

    public void setEmployee(List staff) {
        this.employee = employee;
    }
    
    public String toString() {
        return "[Company name : " + this.name + " chief : " + this.chief + " staff : " + this.employee + "]";
    }    
}
