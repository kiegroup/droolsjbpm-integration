package org.drools.simulation.fluent.test;

public interface RuleTestFluent<T> {

    T assertRuleFired(String ruleName);

    /**
     * Only applies to the last {@link #fireAllRules()} in this step.
     * @param ruleName never null
     * @param fireCount at least 0
     * @return this
     * throws IllegalArgumentException if {@link #fireAllRules()} has not been called in this step yet.
     */
    T assertRuleFired(String ruleName, int fireCount);
     
}
