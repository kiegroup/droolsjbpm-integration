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

package org.drools.benchmark;

import org.kie.internal.KnowledgeBase;

import java.util.concurrent.Callable;

import static org.drools.benchmark.util.DroolsUtil.createKnowledgeBase;
import static org.drools.benchmark.util.DroolsUtil.createKnowledgeBuilder;
import static org.drools.benchmark.util.MemoryUtil.aggressiveGC;
import static org.drools.benchmark.util.MemoryUtil.usedMemory;

public class ObjectSizeCalculator {

    private static final int INSTANCES_NR = 10000;

    public static void main(String[] args) {
        long size = new ObjectSizeCalculator().calcSize();
        System.out.println(size);
    }

    private long calcSize() {
        Object[] objs = new Object[INSTANCES_NR];
        aggressiveGC(10);
        long startMemoryUse = usedMemory();
        try {
            for (int i = 0; i < INSTANCES_NR; i++) objs[i] = OBJECT_GENERATOR.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        aggressiveGC(10);
        long endMemoryUse = usedMemory();
        return (endMemoryUse - startMemoryUse) / INSTANCES_NR;
    }

    private static final Callable<Object> OBJECT_GENERATOR = new StatlessSessionGenerator();

    private static class StatlessSessionGenerator implements Callable<Object> {

        private KnowledgeBase kbase;
        private int idCounter = 0;

        public StatlessSessionGenerator() {
            kbase = createKnowledgeBase(createKnowledgeBuilder(this, "licenseApplication.drl"));
        }

        public Object call() throws Exception {
            return kbase.newStatefulKnowledgeSession();
        }
    }
}
