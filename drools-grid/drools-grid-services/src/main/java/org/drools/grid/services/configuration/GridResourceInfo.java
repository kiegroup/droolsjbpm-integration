package org.drools.grid.services.configuration;

import java.io.Serializable;

public class GridResourceInfo
    implements
    Serializable {
    private Status status;
    private String address;
    private int    port;

    public GridResourceInfo() {
    }

    public GridResourceInfo(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    static public enum Status {
        RUNNING, MISSING
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
