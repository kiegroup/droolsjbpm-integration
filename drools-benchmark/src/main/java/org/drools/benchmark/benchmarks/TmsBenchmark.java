/*
 * Copyright 2015 JBoss Inc
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
import org.kie.internal.KnowledgeBase;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.api.runtime.rule.FactHandle;

public class TmsBenchmark extends AbstractBenchmark {

    private String drlFile;

    private StatefulKnowledgeSession ksession;

    public TmsBenchmark(String drlFile) {
        this.drlFile = drlFile;
    }

    @Override
    public void init(BenchmarkDefinition definition) {
        KnowledgeBase kbase = createKnowledgeBase(createKnowledgeBuilder(drlFile));
        ksession = kbase.newStatefulKnowledgeSession();
    }

    public void execute(int repNr) {
        FactHandle fact = ksession.insert(new Integer(0));
        ksession.fireAllRules();
        ksession.retract(fact);
        ksession.fireAllRules();
    }

    @Override
    public void terminate() {
        if (ksession.getFactCount() > 0L) {
            throw new RuntimeException("Still " + ksession.getFactCount() + " facts");
        }
        ksession.dispose(); // Stateful rule session must always be disposed when finished
    }
}
