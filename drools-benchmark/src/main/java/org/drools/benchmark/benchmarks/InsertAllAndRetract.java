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

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.benchmark.*;
import org.drools.benchmark.model.*;
import org.drools.runtime.*;
import org.drools.runtime.rule.FactHandle;

public class InsertAllAndRetract extends AbstractBenchmark {

    private final int objectNumbers;

    private String[] drlFiles;

    private FactHandle[] facts;
    private StatefulKnowledgeSession ksession;

    public InsertAllAndRetract(int objectNumbers) {
        this.objectNumbers = objectNumbers;
    }

    public InsertAllAndRetract(int objectNumbers, String drlFile) {
        this(objectNumbers);
        this.drlFiles = drlFile.split(",");
    }

    public void init(BenchmarkDefinition definition) {
        KnowledgeBase kbase = createKnowledgeBase(createKnowledgeBuilder(drlFiles));
        ksession = kbase.newStatefulKnowledgeSession();
        facts = new FactHandle[objectNumbers];
    }

    public void execute(int repNr) {
        for (int i = 0; i < objectNumbers; i++) {
            facts[i] = ksession.insert(new DummyBean(i));
        }

        ksession.fireAllRules();

        for (FactHandle fact : facts) {
            ksession.retract(fact);
        }
    }
}
