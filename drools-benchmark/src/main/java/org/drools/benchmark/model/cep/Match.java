package org.drools.benchmark.model.cep;

public class Match {
    private final Figure figure;
    private final Letter letter;

    public Match(Figure figure, Letter letter) {
        this.figure = figure;
        this.letter = letter;
    }

    @Override
    public String toString() {
        return "Match{" +
                "figure=" + figure +
                ", letter=" + letter +
                '}';
    }
}
