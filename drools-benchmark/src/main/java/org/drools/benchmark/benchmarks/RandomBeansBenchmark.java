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
import org.drools.benchmark.model.Bean;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.api.runtime.rule.FactHandle;

public class RandomBeansBenchmark extends AbstractBenchmark {
    private final int beansNumber;

    private KnowledgeBase kbase;

    private StatefulKnowledgeSession ksession;

    private Bean[] beans;
    private FactHandle[] factHandles;

    public RandomBeansBenchmark(int beansNumber, String drlFile) {
        this.beansNumber = beansNumber;
        kbase = createKnowledgeBase(createKnowledgeBuilder(drlFile.split(",")));
    }

    @Override
    public void init(BenchmarkDefinition definition) {
        ksession = kbase.newStatefulKnowledgeSession();
        beans = Bean.generateRandomBeans(beansNumber);
        factHandles = new FactHandle[beans.length];
    }

    public void execute(int repNr) {
        int i = 0;
        for (Bean bean : beans) {
            factHandles[i++] = ksession.insert(bean);
        }
        ksession.fireAllRules();
        for (FactHandle factHandle : factHandles) {
            ksession.retract(factHandle);
        }
        ksession.fireAllRules();
    }

    @Override
    public void terminate() {
        ksession.dispose(); // Stateful rule session must always be disposed when finished
    }
}
