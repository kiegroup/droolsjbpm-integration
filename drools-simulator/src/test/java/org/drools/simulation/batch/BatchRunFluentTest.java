package org.drools.simulation.batch;

import org.drools.compiler.Message;
import org.drools.core.command.RequestContextImpl;
import org.drools.core.command.impl.ContextImpl;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.simulation.fluent.batch.BatchBuilderFluent;
import org.drools.simulation.fluent.batch.ContextBatchBuilderFluent;
import org.drools.simulation.fluent.batch.impl.*;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.drools.compiler.CommonTestMethodBase;
import org.kie.internal.fluent.Scope;

import java.util.Map;

import static org.drools.core.util.DroolsTestUtil.rulestoMap;

public class BatchRunFluentTest extends CommonTestMethodBase {


    @Test
    public void test1() {
        String header = "package org.drools.compiler\n" +
                        "import " + Message.class.getCanonicalName() + "\n";

        String drl1 = "rule R1 when\n" +
                      "   $s : String()\n" +
                      "then\n" +
                      "    System.out.println($s);\n" +
                      "end\n";

        KieServices  ks         = KieServices.Factory.get();
        ReleaseId    releaseId = ks.newReleaseId( "org.kie", "test-upgrade", "1" );
        KieModule    km         = createAndDeployJar(ks, releaseId, header, drl1);
        //KieContainer kc         = ks.newKieContainer(km.getReleaseId());

        //BatchRunImpl batchRun = new BatchRunImpl();

        ContextBatchBuilderFluent fr =  new ContextBatchBuilderFluentImpl();

        FluentContext      fluentCtx = new FluentContext();
        BatchBuilderFluent f         = new BatchFluentBuilderImp(fluentCtx); //fr.newBatch();

        f.newApplicationContext("app1").startConversation()
         .getKieContainer(releaseId).newSession()
         .insert("h1").set("h1").out()
         .insert("h2").set("h2", Scope.CONVERSATION)
         .fireAllRules().dispose();


        //f.newApplicationContext("app1")

        PsuedoClockRunner runner = new PsuedoClockRunner();
        ContextImpl app1 = new ContextImpl("app1", null);
        runner.getAppContexts().put("app1", app1);

        RequestContextImpl requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());
        System.out.println( requestContext.getConversationContext().get("h2") );


        //System.out.print(requestContext.());

        //xxxCtx.set();

        //f.after(100).getKieContainer(releaseId).newSession().after(100).insert("hello").dispose();

//        f.after(100).getKieContainer(releaseId).newSession().after(100).end().getKieContainer(null).newSession();
//
//        f.getApplicationContext("ctx1").getKieContainer(releaseId).newSession().set("s1").insert( new Person() ).out("p1");
//
//        f.getKieContainer(releaseId).newSession().end();

//        KieSessionBatchFluent ksf = fr.get((KieSessionBatchFluent<BatchFluentBuilder>) KieSessionBatchFluent.class, "kkk").insert(1);
//        ksf.end();

        //ksf.insert()


        //f.newBatchBuilderFluent("x1").


    }

    public static KieModule createAndDeployJar( KieServices ks,
                                                ReleaseId releaseId,
                                                String... drls ) {
        byte[] jar = createKJar( ks, releaseId, null, drls );
        return deployJar( ks, jar );
    }


    public static void createAndDeployAndTest( KieContainer kc,
                                               String version,
                                               String header,
                                               String drls,
                                               String... ruleNames ) {
        if ( ruleNames == null ) {
            ruleNames = new String[ 0 ];
        }
        KieServices ks = KieServices.Factory.get();

        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append( header );
        sbuilder.append( drls );

        ReleaseId releaseId1 = ks.newReleaseId( "org.kie", "test-upgrade", version );
        KieModule km = createAndDeployJar( ks, releaseId1, sbuilder.toString() );

        kc.updateToVersion( km.getReleaseId() );

        KiePackage kpkg = kc.getKieBase().getKiePackage("org.drools.compiler");
        assertEquals( ruleNames.length, kpkg.getRules().size() );
        Map<String, Rule> rules = rulestoMap(kpkg.getRules());

        int i = 0;
        for ( String ruleName : ruleNames ) {
            assertEquals( ruleName, i++, ( (RuleImpl) rules.get(ruleName) ).getLoadOrder());
        }
    }


}
