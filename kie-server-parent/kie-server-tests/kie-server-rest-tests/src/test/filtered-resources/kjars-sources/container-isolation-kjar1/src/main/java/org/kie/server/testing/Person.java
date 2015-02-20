package org.kie.server.testing;

public class Person {
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void updateId() {
        id = "Person from kjar1";
    }
}