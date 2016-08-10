package org.drools.simulation.batch;

import org.drools.compiler.Message;
import org.drools.core.command.RequestContextImpl;
import org.drools.core.command.impl.ContextImpl;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.simulation.fluent.batch.BatchBuilderFluent;
import org.drools.simulation.fluent.batch.ContextBatchBuilderFluent;
import org.drools.simulation.fluent.batch.KieSessionBatchFluent;
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
import org.kie.internal.fluent.runtime.KieSessionFluent;

import java.util.Map;

import static org.drools.core.util.DroolsTestUtil.rulestoMap;

public class BatchRunFluentTest extends CommonTestMethodBase {
    String header = "package org.drools.compiler\n" +
                    "import " + Message.class.getCanonicalName() + "\n";

    String drl1 = "global String outS;\n" +
                  "global Long timeNow;\n" +
                  "rule R1 when\n" +
                  "   s : String()\n" +
                  "then\n" +
                  "    kcontext.getKnowledgeRuntime().setGlobal(\"outS\", s);\n" +
                  "    kcontext.getKnowledgeRuntime().setGlobal(\"timeNow\", kcontext.getKnowledgeRuntime().getSessionClock().getCurrentTime() );\n" +
                  "end\n";

    KieServices  ks         = KieServices.Factory.get();
    ReleaseId    releaseId = ks.newReleaseId( "org.kie", "test-upgrade", "1" );
    KieModule    km         = createAndDeployJar(ks, releaseId, header, drl1);

    @Test
    public void testOutName() {
        PsuedoClockRunner runner = new PsuedoClockRunner();

        FluentContext      fluentCtx = new FluentContext();
        BatchBuilderFluent f         = new BatchFluentBuilderImp(fluentCtx);

        f.newApplicationContext("app1")
         .getKieContainer(releaseId).newSession()
         .insert("h1")
         .fireAllRules()
         .getGlobal("outS").out("outS")
         .dispose();

        RequestContextImpl requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());

        assertEquals("h1", requestContext.getOut().get("outS"));
    }


    @Test
    public void testOutWithPriorSetAndNoName() {
        PsuedoClockRunner runner = new PsuedoClockRunner();

        FluentContext      fluentCtx = new FluentContext();
        BatchBuilderFluent f         = new BatchFluentBuilderImp(fluentCtx);

        f.newApplicationContext("app1")
         .getKieContainer(releaseId).newSession()
         .insert("h1")
         .fireAllRules()
         .getGlobal("outS").set("outS").out()
         .dispose();

        RequestContextImpl requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());

        assertEquals("h1", requestContext.getOut().get("outS"));
        assertEquals("h1", requestContext.get("outS"));
    }

    @Test
    public void testOutWithoutPriorSetAndNoName() {
        PsuedoClockRunner runner = new PsuedoClockRunner();

        FluentContext      fluentCtx = new FluentContext();
        BatchBuilderFluent f         = new BatchFluentBuilderImp(fluentCtx);

        f.newApplicationContext("app1")
         .getKieContainer(releaseId).newSession()
         .insert("h1")
         .fireAllRules()
         .getGlobal("outS").out()
         .dispose();

        try {
            RequestContextImpl requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());

            assertEquals("h1", requestContext.getOut().get("out1"));
            fail("Must throw Exception, as no prior set was called and no name given to out");
        } catch ( Exception e ) {

        }
    }

    @Test
    public void testSetAndGetWithCommandRegisterWithEnds() {
        PsuedoClockRunner runner = new PsuedoClockRunner();

        FluentContext      fluentCtx = new FluentContext();
        BatchBuilderFluent f         = new BatchFluentBuilderImp(fluentCtx);

        f.newApplicationContext("app1")
         // create two sessions, and assign names
         .getKieContainer(releaseId).newSession().set("s1").end()
         .getKieContainer(releaseId).newSession().set("s2").end()
         // initialise s1 with data
         .get("s1", KieSessionBatchFluent.class)
         .insert("h1").fireAllRules().end()

         // initialise s2 with data
         .get("s2", KieSessionBatchFluent.class)
         .insert("h2").fireAllRules().end()

         // assign s1 to out
         .get("s1", KieSessionBatchFluent.class)
         .getGlobal("outS").out("outS1").dispose()

         .get("s2", KieSessionBatchFluent.class)
         .getGlobal("outS").out("outS2").dispose();

        RequestContextImpl requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());

        // Check that nothing went to the 'out'
        assertEquals("h1", requestContext.getOut().get("outS1"));
        assertEquals("h2", requestContext.getOut().get("outS2"));
    }

    @Test
    public void testSetAndGetWithCommandRegisterWithoutEnds() {
        PsuedoClockRunner runner = new PsuedoClockRunner();

        FluentContext      fluentCtx = new FluentContext();
        BatchBuilderFluent f         = new BatchFluentBuilderImp(fluentCtx);

        f.newApplicationContext("app1")
         // create two sessions, and assign names
         .getKieContainer(releaseId).newSession().set("s1").end() // this end is needed, it's the get(String, Class) we are checking to see if it auto ends
         .getKieContainer(releaseId).newSession().set("s2")
         // initialise s1 with data
         .get("s1", KieSessionBatchFluent.class)
         .insert("h1").fireAllRules()

         // initialise s2 with data
         .get("s2", KieSessionBatchFluent.class)
         .insert("h2").fireAllRules()

         // assign s1 to out
         .get("s1", KieSessionBatchFluent.class)
         .getGlobal("outS").out("outS1").dispose()

         .get("s2", KieSessionBatchFluent.class)
         .getGlobal("outS").out("outS2").dispose();

        RequestContextImpl requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());

        // Check that nothing went to the 'out'
        assertEquals("h1", requestContext.getOut().get("outS1"));
        assertEquals("h2", requestContext.getOut().get("outS2"));
    }


    @Test
    public void testConversationIdIncreases() {
        PsuedoClockRunner runner = new PsuedoClockRunner();

        FluentContext      fluentCtx = new FluentContext();
        BatchBuilderFluent f         = new BatchFluentBuilderImp(fluentCtx);

        f.newApplicationContext("app1").startConversation()
         .getKieContainer(releaseId).newSession()
         .insert("h1")
         .fireAllRules()
         .dispose();

        RequestContextImpl requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());

        long conversationId = requestContext.getConversationContext().getConversationId();
        assertEquals(0, conversationId);

        requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());

        conversationId = requestContext.getConversationContext().getConversationId();
        assertEquals(1, conversationId);
    }

    @Test
    public void testRequestScope() {
        PsuedoClockRunner runner = new PsuedoClockRunner();

        FluentContext      fluentCtx = new FluentContext();
        BatchBuilderFluent f         = new BatchFluentBuilderImp(fluentCtx);

        f.newApplicationContext("app1")
         .getKieContainer(releaseId).newSession()
         .insert("h1")
         .fireAllRules()
         .getGlobal("outS").set("outS1") // Request is default
         .dispose();

        RequestContextImpl requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());

        // Check that nothing went to the 'out'
        assertNull(requestContext.getOut().get("outS"));
        assertNull(requestContext.getApplicationContext().get("outS1") );
        assertNull(requestContext.getConversationContext() );
        assertEquals("h1", requestContext.get("outS1") );
    }

    @Test
    public void testApplicationScope() {
        PsuedoClockRunner runner = new PsuedoClockRunner();

        FluentContext      fluentCtx = new FluentContext();
        BatchBuilderFluent f         = new BatchFluentBuilderImp(fluentCtx);

        f.newApplicationContext("app1")
         .getKieContainer(releaseId).newSession()
         .insert("h1")
         .fireAllRules()
         .getGlobal("outS").set("outS1", Scope.APPLICATION)
         .dispose();

        RequestContextImpl requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());

        // Check that nothing went to the 'out'
        assertEquals(null, requestContext.getOut().get("outS"));
        assertEquals("h1", requestContext.getApplicationContext().get("outS1") );

        // Make another request, add to application context, assert old and new values are there.
        fluentCtx = new FluentContext();
        f         = new BatchFluentBuilderImp(fluentCtx);

        f.getApplicationContext("app1")
         .getKieContainer(releaseId).newSession()
         .insert("h2")
         .fireAllRules()
         .getGlobal("outS").set("outS2", Scope.APPLICATION)
         .dispose();

        requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());
        assertEquals("h1", requestContext.getApplicationContext().get("outS1") );
        assertEquals("h2", requestContext.getApplicationContext().get("outS2") );
    }


    @Test
    public void testConversationScope() {
        PsuedoClockRunner runner = new PsuedoClockRunner();

        FluentContext      fluentCtx = new FluentContext();
        BatchBuilderFluent f         = new BatchFluentBuilderImp(fluentCtx);

        f.newApplicationContext("app1").startConversation()
         .getKieContainer(releaseId).newSession()
         .insert("h1")
         .fireAllRules()
         .getGlobal("outS").set("outS1", Scope.CONVERSATION)
         .dispose();

        RequestContextImpl requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());

        // check that nothing went to the 'out'
        assertEquals(null, requestContext.getOut().get("outS"));

        long conversationId = requestContext.getConversationContext().getConversationId();

        assertEquals("h1", requestContext.getConversationContext().get("outS1") );

        // Make another request, add to conversation context, assert old and new values are there.
        fluentCtx = new FluentContext();
        f         = new BatchFluentBuilderImp(fluentCtx);

        f.getApplicationContext("app1").joinConversation(conversationId)
         .getKieContainer(releaseId).newSession()
         .insert("h2")
         .fireAllRules()
         .getGlobal("outS").set("outS2", Scope.CONVERSATION)
         .dispose();

        requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());
        assertEquals("h1", requestContext.getConversationContext().get("outS1") );
        assertEquals("h2", requestContext.getConversationContext().get("outS2") );

        // End the conversation, check it's now null
        fluentCtx = new FluentContext();
        f         = new BatchFluentBuilderImp(fluentCtx);

        f.endConversation(conversationId);

        requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());
        assertNull(requestContext.getConversationContext());
    }

    @Test
    public void testContextScopeSearching() {
        PsuedoClockRunner runner = new PsuedoClockRunner();

        FluentContext      fluentCtx = new FluentContext();
        BatchBuilderFluent f         = new BatchFluentBuilderImp(fluentCtx);

        // Check that get() will search up to Application, when no request or conversation values
        f.newApplicationContext("app1")
         .getKieContainer(releaseId).newSession()
         .insert("h1")
         .fireAllRules()
         .getGlobal("outS").set("outS1", Scope.APPLICATION)
         .get("outS1").out()
         .dispose();
        RequestContextImpl requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());

        assertEquals("h1", requestContext.getOut().get("outS1"));
        assertEquals("h1", requestContext.getApplicationContext().get("outS1") );
        assertEquals("h1", requestContext.get("outS1") );

        // Check that get() will search up to Conversation, thus over-riding Application scope and ignoring Request when it has no value
        fluentCtx = new FluentContext();
        f         = new BatchFluentBuilderImp(fluentCtx);

        f.getApplicationContext("app1").startConversation()
         .getKieContainer(releaseId).newSession()
         .insert("h2")
         .fireAllRules()
         .getGlobal("outS").set("outS1", Scope.CONVERSATION)
         .get("outS1").out()
         .dispose();
        requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());

        assertEquals("h2", requestContext.getOut().get("outS1"));
        assertEquals("h1", requestContext.getApplicationContext().get("outS1") );
        assertEquals("h2", requestContext.getConversationContext().get("outS1") );
        assertEquals("h2", requestContext.get("outS1") );


        // Check that get() will search directly to Request, thus over-riding Application and Conversation scoped values
        fluentCtx = new FluentContext();
        f         = new BatchFluentBuilderImp(fluentCtx);

        f.getApplicationContext("app1").joinConversation(requestContext.getConversationContext().getConversationId())
         .getKieContainer(releaseId).newSession()
         .insert("h3")
         .fireAllRules()
         .getGlobal("outS").set("outS1", Scope.REQUEST)
         .get("outS1").out()
         .dispose();
        requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());

        assertEquals("h3", requestContext.getOut().get("outS1"));
        assertEquals("h1", requestContext.getApplicationContext().get("outS1") );
        assertEquals("h2", requestContext.getConversationContext().get("outS1") );
        assertEquals("h3", requestContext.get("outS1") );
    }




    @Test
    public void testAfter() {
        PsuedoClockRunner runner = new PsuedoClockRunner(0);

        FluentContext      fluentCtx = new FluentContext();
        BatchBuilderFluent f         = new BatchFluentBuilderImp(fluentCtx);

        // Check that get() will search up to Application, when no request or conversation values
        f.after(1000).newApplicationContext("app1")
         .getKieContainer(releaseId).newSession()
         .insert("h1")
         .fireAllRules()
         .getGlobal("outS").out("outS1")
         .getGlobal("timeNow").out("timeNow1")
         .dispose()
         .after(2000).newApplicationContext("app1")
         .getKieContainer(releaseId).newSession()
         .insert("h1")
         .fireAllRules()
         .getGlobal("outS").out("outS2")
         .getGlobal("timeNow").out("timeNow2")
         .dispose();

        RequestContextImpl requestContext = (RequestContextImpl) runner.execute(fluentCtx.getBatches());

        assertEquals(1000l, requestContext.getOut().get("timeNow1"));
        assertEquals(2000l, requestContext.getOut().get("timeNow2"));
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
