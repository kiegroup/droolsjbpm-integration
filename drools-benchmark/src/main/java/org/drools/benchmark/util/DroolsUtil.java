package org.drools.benchmark.util;

import org.kie.KnowledgeBase;
import org.kie.KnowledgeBaseFactory;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.KnowledgeBuilderFactory;
import org.kie.io.ResourceFactory;
import org.kie.io.ResourceType;

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

    public static KnowledgeBase createKnowledgeBase(KnowledgeBuilder kbuilder) {
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        if (kbuilder != null) kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        return kbase;
    }

    public static KnowledgeBase createKnowledgeBase(String drl) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newByteArrayResource(drl.getBytes()), ResourceType.DRL );

        if (kbuilder.hasErrors()) {
            throw new RuntimeException(kbuilder.getErrors().toString());
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
        return kbase;
    }
}