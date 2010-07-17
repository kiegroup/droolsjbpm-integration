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
