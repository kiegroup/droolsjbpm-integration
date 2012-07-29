package org.drools.benchmark.model;

public class A {

    private int i;
    private int j;

    public A(int i) {
        this(i, i);
    }

    public A(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }
}
