/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import org.drools.benchmark.BenchmarkDefinition;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

public abstract class AgendaBenchmark extends AbstractBenchmark {

    protected final int rulesNr;

    private static final String RULE =
            "rule R%i salience %i\n" +
            "when\n" +
            "    $a : Integer( intValue == %i )\n" +
            "then\n" +
            "end\n\n";

    protected KieSession ksession;
    protected FactHandle[] facts;
    private KieBase kbase;

    public AgendaBenchmark(int rulesNr) {
        this.rulesNr = rulesNr;
        String drl = repeatPatternString(RULE, rulesNr);
        kbase = createKnowledgeBase(drl);
    }

    @Override
    public void init(BenchmarkDefinition definition) {
        ksession = kbase.newKieSession();
        facts = new FactHandle[rulesNr];
    }

    @Override
    public void terminate() {
        ksession.dispose(); // Stateful rule session must always be disposed when finished
    }
}
