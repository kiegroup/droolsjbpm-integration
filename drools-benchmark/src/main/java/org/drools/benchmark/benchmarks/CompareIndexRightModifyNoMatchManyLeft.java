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

public class CompareIndexRightModifyNoMatchManyLeft extends AbstractBenchmark {

    private final int objectNr;
    private final int modifications;

    private final String drlFile;

    private StatefulKnowledgeSession ksession;

    private Random random = new Random(0);

    private A[] as;
    private B[] bs;

    public CompareIndexRightModifyNoMatchManyLeft(int aNr, int modifications, String drlFile) {
        this.objectNr = aNr;
        this.modifications = modifications;
        this.drlFile = drlFile;
    }

    @Override
    public void init(BenchmarkDefinition definition) {
        KnowledgeBase kbase = createKnowledgeBase(createKnowledgeBuilder(drlFile));
        ksession = kbase.newStatefulKnowledgeSession();

        int aLimit = objectNr * modifications;

        as = new A[objectNr];
        for (int i = 0; i < objectNr; i++) {
            as[i] = new A(random.nextInt(aLimit));
        }

        bs = new B[objectNr];
        for (int i = 0; i < objectNr; i++) {
            int randomInt = random.nextInt(aLimit) + (aLimit * 2);
            bs[i] = new B(randomInt, randomInt+modifications);
        }
    }

    public void execute(int repNr) {
        for (int i = 0; i < objectNr; i++) {
            ksession.insert(as[i]);
            ksession.insert(bs[i]);
        }
        ksession.fireAllRules();
    }

    @Override
    public void terminate() {
        ksession.dispose(); // Stateful rule session must always be disposed when finished
    }
}
