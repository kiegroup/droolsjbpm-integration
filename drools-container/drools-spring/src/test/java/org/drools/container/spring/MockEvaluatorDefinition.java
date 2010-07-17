package org.drools.container.spring;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.drools.base.ValueType;
import org.drools.base.evaluators.EvaluatorDefinition;
import org.drools.base.evaluators.Operator;
import org.drools.spi.Evaluator;

public class MockEvaluatorDefinition implements EvaluatorDefinition {

    public Evaluator getEvaluator(ValueType type,
                                  String operatorId,
                                  boolean isNegated,
                                  String parameterText,
                                  Target leftTarget,
                                  Target rightTarget) {
        return null;
    }

    public Evaluator getEvaluator(ValueType type,
                                  String operatorId,
                                  boolean isNegated,
                                  String parameterText) {
        return null;
    }

    public Evaluator getEvaluator(ValueType type,
                                  Operator operator,
                                  String parameterText) {
        return null;
    }

    public Evaluator getEvaluator(ValueType type,
                                  Operator operator) {
        return null;
    }

    public String[] getEvaluatorIds() {
        return new String[] { "id1", "id2" };
    }

    public Target getTarget() {
        return null;
    }

    public boolean isNegatable() {
        return false;
    }

    public boolean supportsType(ValueType type) {
        return false;
    }

    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
  
    }

    public void writeExternal(ObjectOutput out) throws IOException {

    }

}
