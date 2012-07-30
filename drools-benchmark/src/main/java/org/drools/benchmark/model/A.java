package org.drools.benchmark.model;

public class A {

    private boolean b;
    private int i;
    private int j;

    public A(int i) {
        this(i, i);
    }

    public A(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public A(int i, int j, boolean b) {
        this.i = i;
        this.j = j;
        this.b = b;
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

    public boolean isB() {
        return b;
    }

    public void setB(boolean b) {
        this.b = b;
    }
}
