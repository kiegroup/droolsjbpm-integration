package org.drools.camel.example;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelSpringJUnit4ClassRunner;
import org.kie.internal.KnowledgeBase;
import org.kie.definition.type.FactType;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/test-mortgage-context.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class GuvnorRulesMortgageTest {

	@EndpointInject(uri = "mock:result")
	protected MockEndpoint resultEndpoint;

	@Produce(uri = "direct:insert")
	protected ProducerTemplate produceFacts;

    @Produce(uri = "direct:execute")
    protected ProducerTemplate produceExecute;

    @Resource(name = "kbase1")
    protected KnowledgeBase kBase;

    @Resource(name = "ksession1")
    protected StatefulKnowledgeSession ksession;

	@Test
	public void testDirect() throws Exception {

        // Dynamic fact creation as the model was declared in the DRL
        FactType appType = kBase.getFactType("mortgages", "LoanApplication");
        Object application = appType.newInstance();
        appType.set(application, "amount", 25000);
        appType.set(application, "deposit", 1500);
        appType.set(application, "lengthYears", 20);

        FactType incomeType = kBase.getFactType("mortgages", "IncomeSource");
        Object income = incomeType.newInstance();
        incomeType.set(income, "type", "Job");
        incomeType.set(income, "amount", 65000);

        //ksession.fireAllRules();
        //System.out.println(">> Result : " + application);

        // Inject LoanApplication & Income
        produceFacts.sendBody(application);

        // Fire commands and send Application fact that we would like to calculate
        ExecutionResultImpl result = produceExecute.requestBody(application, ExecutionResultImpl.class);

        // Expecting single result value of type LoanApplication
        Collection<String> identifiers = result.getIdentifiers();
        assertNotNull(identifiers);
        assertTrue(identifiers.size() >= 0);

        for (String identifier : identifiers) {
            final Object value = result.getValue(identifier);
            assertNotNull(value);

            Class clazz = value.getClass();
            assertEquals("mortgages.LoanApplication",clazz.getName());

            Field approved = value.getClass().getDeclaredField("approved");
            approved.setAccessible(true);
            assertEquals(false, approved.get(value));

            Field approvedRate = value.getClass().getDeclaredField("approvedRate");
            approvedRate.setAccessible(true);
            assertEquals(0,approvedRate.get(value));
        }



	}

}
