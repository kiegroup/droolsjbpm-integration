package org.kie.server.testing.multimodule.domain;

public class Bus implements Vehicle {
    private String message;

    @Override
    public void drive() {
        message = "Driving bus!";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}