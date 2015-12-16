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

import org.drools.core.common.InternalFactHandle;
import org.kie.api.runtime.rule.FactHandle;

public class AgendaInsertUpdateBenchmark extends AgendaBenchmark {

    public AgendaInsertUpdateBenchmark(int rulesNr) {
        super(rulesNr);
    }

    public void execute(int repNr) {
        for (int i = 0; i < rulesNr; i++) {
            facts[i] = ksession.insert(new Integer(i));
        }
        for (FactHandle fact : facts) {
            int oldInt = (Integer)((InternalFactHandle)fact).getObject();
            ksession.update(fact, new Integer(oldInt+1));
        }
    }


    @Override
    public void terminate() {
        for (FactHandle fact : facts) {
            ksession.retract(fact);
        }
        super.terminate();
    }
}
