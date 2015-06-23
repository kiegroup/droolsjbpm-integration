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
import org.drools.benchmark.model.A;
import org.drools.benchmark.model.B;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import java.util.Random;

public class CompareIndexRightModifyNoMatchOneLeft extends AbstractBenchmark {

    private final int aNr;
    private final int modifications;

    private final String drlFile;

    private StatefulKnowledgeSession ksession;

    private Random random = new Random(0);

    private B[] bs;

    public CompareIndexRightModifyNoMatchOneLeft(int aNr, int modifications, String drlFile) {
        this.aNr = aNr;
        this.modifications = modifications;
        this.drlFile = drlFile;
    }

    @Override
    public void init(BenchmarkDefinition definition) {
        KnowledgeBase kbase = createKnowledgeBase(createKnowledgeBuilder(drlFile));
        ksession = kbase.newStatefulKnowledgeSession();

        bs = new B[aNr];
        for (int i = 0; i < aNr; i++) {
            int randomInt = random.nextInt();
            bs[i] = new B(randomInt, randomInt+modifications);
        }
    }

    public void execute(int repNr) {
        for (int i = 0; i < aNr; i++) {
            ksession.insert(bs[i]);
        }
        ksession.insert(new A(Integer.MIN_VALUE));
        ksession.fireAllRules();
    }

    @Override
    public void terminate() {
        ksession.dispose(); // Stateful rule session must always be disposed when finished
    }
}
