package org.kie.server.testing;

public class Person {
    private Integer age;
    private Boolean canBuyAlcohol;

    public Person() {
    }

    public Person(Integer age) {
        this.age = age;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
    
    public Boolean canBuyAlcohol() {
        return canBuyAlcohol;
    }

    public void setCanBuyAlcohol(Boolean canBuyAlcohol) {
        this.canBuyAlcohol = canBuyAlcohol;
    }
}
