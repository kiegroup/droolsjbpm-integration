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

package org.drools.benchmark.util;

import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;

public class DroolsUtil {

    public static KnowledgeBuilder createKnowledgeBuilder(Object invoker, String... drlFiles) {
        if (drlFiles == null) return null;
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        for (String drlFile : drlFiles) {
            kbuilder.add(ResourceFactory.newClassPathResource(drlFile, invoker.getClass()), ResourceType.DRL);
        }
        if (kbuilder.hasErrors()) {
            throw new RuntimeException(kbuilder.getErrors().toString());
        }
        return kbuilder;
    }

    public static KieBase createKnowledgeBase(KnowledgeBuilder kbuilder) {
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        if (kbuilder != null) kbase.addPackages(kbuilder.getKnowledgePackages());
        return kbase;
    }

    public static KieBase createKnowledgeBase(String drl) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newByteArrayResource(drl.getBytes()), ResourceType.DRL );

        if (kbuilder.hasErrors()) {
            throw new RuntimeException(kbuilder.getErrors().toString());
        }

        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages( kbuilder.getKnowledgePackages() );
        return kbase;
    }
}
