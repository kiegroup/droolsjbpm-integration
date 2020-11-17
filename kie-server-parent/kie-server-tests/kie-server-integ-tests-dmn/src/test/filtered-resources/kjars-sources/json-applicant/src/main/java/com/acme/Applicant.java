package com.acme;

public class Applicant {

    private String name;
    private int age;
    private Address address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Applicant() {

    }

    public Applicant(String name, Integer age, Address address) {
        this.name = name;
        this.age = age;
        this.address = address;
    }


}