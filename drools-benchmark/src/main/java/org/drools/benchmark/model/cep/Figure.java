package org.drools.benchmark.model.cep;

public class Figure {
    private final int key;
    private final int value1;

    public Figure(int key, int value1) {
        this.key = key;
        this.value1 = value1;
    }

    public int getKey() {
        return key;
    }

    public int getValue1() {
        return value1;
    }

    @Override
    public String toString() {
        return "Figure{" +
                "key=" + key +
                ", value1=" + value1 +
                '}';
    }
}
