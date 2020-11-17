package com.acme;

public class Address {
    private String country;
    private String zip;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public Address(){

    }

    public Address(String country, String zip) {
        this.country = country;
        this.zip = zip;
    }
}
