package org.drools.benchmark.model;

public class B {

    private int i;
    private int j;

    public B(int i) {
        this(i, i);
    }

    public B(int i, int j) {
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
