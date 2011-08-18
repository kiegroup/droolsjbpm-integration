/*
 * Copyright 2011 JBoss Inc
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

package org.drools.benchmark.benchmarks;

import org.drools.*;
import org.drools.benchmark.*;
import org.drools.benchmark.model.*;
import org.drools.runtime.*;

public class FibonacciBenchmark extends AbstractBenchmark {

    private int number;
    private String drlFile;

    private StatefulKnowledgeSession ksession;

    public FibonacciBenchmark(int number) {
        this(number, "fibonacci.drl");
    }

    public FibonacciBenchmark(int number, String drlFile) {
        this.number = number;
        this.drlFile = drlFile;
    }

    @Override
    public void init(BenchmarkDefinition definition) {
        KnowledgeBase kbase = createKnowledgeBase(createKnowledgeBuilder(drlFile));
        ksession = kbase.newStatefulKnowledgeSession();
        ksession.insert(new Fibonacci(number));
    }

    public void execute(int repNr) {
        ksession.fireAllRules();
    }

    @Override
    public void terminate() {
        ksession.dispose(); // Stateful rule session must always be disposed when finished
    }
}
