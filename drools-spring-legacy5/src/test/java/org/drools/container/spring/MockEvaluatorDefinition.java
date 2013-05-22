/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.container.spring;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.EvaluatorDefinition;
import org.drools.core.base.evaluators.Operator;
import org.drools.core.spi.Evaluator;

public class MockEvaluatorDefinition
    implements
    EvaluatorDefinition {

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
        return new String[]{"id1", "id2"};
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
