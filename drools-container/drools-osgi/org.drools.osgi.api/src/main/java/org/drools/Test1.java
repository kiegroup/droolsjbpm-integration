package org.drools;

import org.drools.builder.KnowledgeBuilderProvider;

public class Test1 {
    public static void test() {
        try {
            Class<KnowledgeBuilderProvider> cls = (Class<KnowledgeBuilderProvider>) Class.forName( "org.drools.builder.impl.KnowledgeBuilderProviderImpl", true, Test1.class.getClassLoader() );
            System.out.println( cls );
        } catch ( Exception e2 ) {
            throw new RuntimeException( e2 );
        }        
    }
}
