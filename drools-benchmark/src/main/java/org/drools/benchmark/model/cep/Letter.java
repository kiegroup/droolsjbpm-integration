package org.drools.benchmark.model.cep;

public class Letter {
    private final int key;
    private final char value;

    public Letter(int key, char value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public char getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Letter{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
