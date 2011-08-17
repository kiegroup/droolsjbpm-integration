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

package org.drools.benchmark.benchmarks;

import org.drools.*;
import org.drools.benchmark.*;
import org.drools.benchmark.model.*;
import org.drools.runtime.*;
import org.drools.runtime.rule.*;
import org.drools.runtime.rule.FactHandle;

/**
 * @author Mario Fusco
 */
public class InsertAndRetractInTurn extends AbstractBenchmark {

    private StatefulKnowledgeSession ksession;

    private String[] drlFiles;

    public InsertAndRetractInTurn() { }

    public InsertAndRetractInTurn(String drlFile) {
        this.drlFiles = drlFile.split(",");
    }

    public void init(BenchmarkDefinition definition) {
        KnowledgeBase kbase = createKnowledgeBase(createKnowledgeBuilder(drlFiles));
        ksession = kbase.newStatefulKnowledgeSession();
    }

    public void execute(int repNr) {
        FactHandle fact = ksession.insert(new DummyBean(repNr));
        ksession.fireAllRules();
        ksession.retract(fact);
    }
}
