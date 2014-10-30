package org.kie.server.testing.multimodule.domain;

public class Car implements Vehicle {
    private String message;

    @Override
    public void drive() {
        message = "Driving car!";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}