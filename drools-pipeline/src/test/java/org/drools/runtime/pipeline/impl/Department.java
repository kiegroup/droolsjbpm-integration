package org.drools.runtime.pipeline.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample Department bean to demostrate main excel export features
 * author: Leonid Vysochyn
 */
public class Department {
    private String name;
    private Employee chief = new Employee();
    private List staff = new ArrayList();
    
    public Department() {
    }

    public Department(String name) {
        this.name = name;
    }

    public Department(String name, Employee chief, List staff) {
        this.name = name;
        this.chief = chief;
        this.staff = staff;
    }

    public void addEmployee(Employee employee) {
        this.staff.add(employee);
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

    public List getStaff() {
        return staff;
    }

    public void setStaff(List staff) {
        this.staff = staff;
    }
    
    public String toString() {
        return "[Department name : " + this.name + " chief : " + this.chief + " staff : " + this.staff + "]";
    }
}
