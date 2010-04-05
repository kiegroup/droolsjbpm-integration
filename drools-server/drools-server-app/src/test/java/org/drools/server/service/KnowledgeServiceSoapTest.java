package org.drools.server.service;

import org.drools.runtime.ExecutionResults;
import org.drools.runtime.help.BatchExecutionHelper;
import org.drools.runtime.rule.impl.FlatQueryResults;
import org.drools.server.KnowledgeServiceBaseTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:configuration-test.xml" })
public class KnowledgeServiceSoapTest extends KnowledgeServiceBaseTest {

	@Autowired
	@Qualifier("knowledgeServiceSoapImpl")
	protected KnowledgeServiceSoap proxy;

	@Test
	public void testJaxbInsertCommand() throws Exception {
		String cmd = getJaxbCommand();

		String response = proxy.execute(cmd);

		assertTrue(response.indexOf("<ns2:name>santa</ns2:name>") > -1);
		assertTrue(response.indexOf("<item key=\"lucaz\">\n            <value xsi:type=\"disconnectedFactHandle\"") > -1);
		assertTrue(response.indexOf("<item key=\"baunax\">\n            <value xsi:type=\"disconnectedFactHandle\"") > -1);
	}

	@Test
	public void testXStreamInsertCommand() throws Exception {
		String cmd = getXStreamCommand();
		String response = proxy.execute(cmd);

		ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML(response);

		assertNotNull(result.getFactHandle("lucaz"));

		FlatQueryResults personsQuery = (FlatQueryResults) result.getValue("persons");
		assertEquals(2, personsQuery.size());
	}

	@Test
	public void testBothsessions() throws Exception {
		String cmd = getJaxbCommand();

		String response = proxy.execute(cmd);

		assertTrue(response.indexOf("<ns2:name>santa</ns2:name>") > -1);
		assertTrue(response.indexOf("<item key=\"lucaz\">\n            <value xsi:type=\"disconnectedFactHandle\"") > -1);
		assertTrue(response.indexOf("<item key=\"baunax\">\n            <value xsi:type=\"disconnectedFactHandle\"") > -1);

		cmd = getXStreamCommand();

		response = proxy.execute(cmd);

		ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML(response);

		assertNotNull(result.getFactHandle("lucaz"));

		FlatQueryResults personsQuery = (FlatQueryResults) result.getValue("persons");
		assertEquals(2, personsQuery.size());
	}

}
