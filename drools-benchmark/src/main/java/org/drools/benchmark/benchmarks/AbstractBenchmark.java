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
import org.drools.builder.*;
import org.drools.io.*;

public abstract class AbstractBenchmark implements Benchmark {

    protected final String LINE_SEPARATOR = System.getProperty("line.separator");

    public void init(BenchmarkDefinition definition) { }
    public void terminate() { }

    protected final KnowledgeBuilder createKnowledgeBuilder(String... drlFiles) {
        if (drlFiles == null) return null;
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        for (String drlFile : drlFiles)
            kbuilder.add(ResourceFactory.newClassPathResource(drlFile, getClass()), ResourceType.DRL);
        if (kbuilder.hasErrors()) throw new RuntimeException(kbuilder.getErrors().toString());
        return kbuilder;
    }

    protected final KnowledgeBase createKnowledgeBase(KnowledgeBuilder kbuilder) {
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        if (kbuilder != null) kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        return kbase;
    }
}
