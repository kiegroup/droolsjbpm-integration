package org.drools.kproject;

import static junit.framework.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.kie.KieBase;
import org.kie.builder.KieContainer;
import org.kie.builder.KieServices;
import org.kie.definition.type.FactType;
import org.kie.runtime.KieSession;

public class KProjectTest {

    @Test @Ignore
    public void testKJar() throws Exception {
        KieServices ks = KieServices.Factory.get();
        KieContainer kContainer = ks.getKieClasspathContainer();
        KieSession kSession = kContainer.getKieSession( "org.test.KSession1" );

        useKSession(kSession);
    }

    private void useKSession(KieSession ksession) throws InstantiationException, IllegalAccessException {
        KieBase kbase = ksession.getKnowledgeBase();
        FactType aType = kbase.getFactType( "org.drools.test", "FactA" );
        Object a = aType.newInstance();
        FactType bType = kbase.getFactType( "org.drools.test", "FactB" );
        Object b = bType.newInstance();
        aType.set( a, "fieldB", b );
        bType.set( b, "fieldA", a );

        ksession.insert( a );
        ksession.insert( b );

        int rules = ksession.fireAllRules();
        assertEquals( 1, rules );
    }
}
