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
import java.util.List;

import org.kie.api.remote.Remotable;

@Remotable
public class Reimbursement implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Employee employee;
    
    private List<Expense> expenses;

    public Reimbursement() {
    }

    public Employee getEmployee() {
        return employee;
    }
    
    public List<Expense> getExpenses() {
        return expenses;
    }
    
    public Reimbursement(Employee employee, List<Expense> expenses) {
        this.employee = employee;
        this.expenses = expenses;
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((employee == null) ? 0 : employee.hashCode());
        result = prime * result + ((expenses == null) ? 0 : expenses.hashCode());
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
        Reimbursement other = (Reimbursement) obj;
        if (employee == null) {
            if (other.employee != null)
                return false;
        } else if (!employee.equals(other.employee))
            return false;
        if (expenses == null) {
            if (other.expenses != null)
                return false;
        } else if (!expenses.equals(other.expenses))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Reimbursement [employee=" + employee + ", expenses=" + expenses + "]";
    }

}