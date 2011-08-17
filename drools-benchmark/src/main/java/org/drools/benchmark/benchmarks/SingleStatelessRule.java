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

import static org.drools.benchmark.model.Gender.*;
import org.drools.benchmark.model.*;

import org.drools.*;
import org.drools.builder.*;
import org.drools.io.*;
import org.drools.runtime.*;

/**
 * @author Mario Fusco
 */
public class SingleStatelessRule extends AbstractBenchmark {

    public void execute(int repNr) {
        KnowledgeBase kbase = createKnowledgeBase(createKnowledgeBuilder("licenseApplication.drl"));

        StatelessKnowledgeSession ksession = kbase.newStatelessKnowledgeSession();
        Applicant applicant = new Applicant("Mr John Smith", 21, MALE);

        if (applicant.isValid()) throw new RuntimeException("Applicant shouldn't be valid");
        ksession.execute(applicant);
        if (!applicant.isValid()) throw new RuntimeException("Applicant should be valid");
    }
}
