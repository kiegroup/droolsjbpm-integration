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

import org.drools.benchmark.*;
import org.drools.benchmark.util.DroolsUtil;
import org.kie.builder.*;
import org.kie.internal.KnowledgeBase;

public abstract class AbstractBenchmark implements Benchmark {

    protected final String LINE_SEPARATOR = System.getProperty("line.separator");

    public void init(BenchmarkDefinition definition) {
        init(definition, true);
    }

    public void init(BenchmarkDefinition definition, boolean isFirst) { }

    public void terminate() {
        terminate(true);
    }

    public void terminate(boolean isLast) { }

    protected final KnowledgeBuilder createKnowledgeBuilder(String... drlFiles) {
        return DroolsUtil.createKnowledgeBuilder(this, drlFiles);
    }

    protected final KnowledgeBase createKnowledgeBase(KnowledgeBuilder kbuilder) {
        return DroolsUtil.createKnowledgeBase(kbuilder);
    }

    protected final KnowledgeBase createKnowledgeBase(String drl) {
        return DroolsUtil.createKnowledgeBase(drl);
    }

    public Benchmark clone() {
        throw new UnsupportedOperationException("Override clone() to allow to run this benchmark in parallel");
    }

    protected final String repeatPatternString(String pattern, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(pattern.replaceAll("%i", "" + i));
        }
        return sb.toString();
    }
}
