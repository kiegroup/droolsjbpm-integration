package org.drools.runtime.pipeline.impl;

import java.util.Date;
import java.util.List;

/**
 * Sample Employee bean to demostrate simple export features
 * @author Leonid Vysochyn
 */
public class Employee {
    private String name;
    private Integer age;
    private Double payment;
    private Double bonus;
    private Date birthDate;
    private Employee superior;
    private List notes;
    private String id;


    public Employee() {
    }

    public List getNotes() {
        return notes;
    }

    public void setNotes(List notes) {
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Employee(String name, Integer age, Double payment, Double bonus) {
        this.name = name;
        this.age = age;
        this.payment = payment;
        this.bonus = bonus;
    }

    public Employee(String name, int age, double payment, double bonus, Date birthDate) {
        this.name = name;
        this.age = new Integer(age);
        this.payment = new Double(payment);
        this.bonus = new Double(bonus);
        this.birthDate = birthDate;
    }

    public Employee(String name, Double payment, Double bonus) {
        this.name = name;
        this.payment = payment;
        this.bonus = bonus;
    }

    public Employee(String name, int age, double payment, double bonus) {
        this.name = name;
        this.age = new Integer(age);
        this.payment = new Double(payment);
        this.bonus = new Double(bonus);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getPayment() {
        return payment;
    }

    public void setPayment(Double payment) {
        this.payment = payment;
    }

    public Double getBonus() {
        return bonus;
    }

    public void setBonus(Double bonus) {
        this.bonus = bonus;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Employee getSuperior() {
        return superior;
    }

    public void setSuperior(Employee superior) {
        this.superior = superior;
    }
    
    public String toString() {
        return "[Employee name : " + this.name + "]"; 
    }

}
