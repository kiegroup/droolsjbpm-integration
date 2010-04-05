package org.drools.server.service;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
public class KnowledgeServiceRestTest extends KnowledgeServiceBaseTest {

	@Autowired
	@Qualifier("knowledgeServiceRestImpl")
	protected KnowledgeServiceRest proxy;

	@Test
	public void testInvalidCommand() throws Exception {
		Response response = proxy.execute("asdsad");
		assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	@Test
	public void testInvalidProfile() throws Exception {
		Response response = proxy.execute("unknow profile");
		assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	@Test
	public void testJaxbInsertCommand() throws Exception {
		String cmd = getJaxbCommand();

		Response response = proxy.execute(cmd);

		assertEquals(200, response.getStatus());

		String XMLoutput = (String) response.getEntity();

		assertTrue(XMLoutput.indexOf("<ns2:name>santa</ns2:name>") > -1);
		assertTrue(XMLoutput.indexOf("<item key=\"lucaz\">\n            <value xsi:type=\"disconnectedFactHandle\"") > -1);
		assertTrue(XMLoutput.indexOf("<item key=\"baunax\">\n            <value xsi:type=\"disconnectedFactHandle\"") > -1);
	}

	@Test
	public void testXStreamInsertCommand() throws Exception {
		String cmd = getXStreamCommand();
		Response response = proxy.execute(cmd);
		assertEquals(200, response.getStatus());

		String XMLoutput = (String) response.getEntity();

		ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML(XMLoutput);

		assertNotNull(result.getFactHandle("lucaz"));

		FlatQueryResults personsQuery = (FlatQueryResults) result.getValue("persons");
		assertEquals(2, personsQuery.size());
	}

	@Test
	public void testBothsessions() throws Exception {
		String cmd = getJaxbCommand();

		Response response = proxy.execute(cmd);

		assertEquals(200, response.getStatus());

		String XMLoutput = (String) response.getEntity();

		assertTrue(XMLoutput.indexOf("<ns2:name>santa</ns2:name>") > -1);
		assertTrue(XMLoutput.indexOf("<item key=\"lucaz\">\n            <value xsi:type=\"disconnectedFactHandle\"") > -1);
		assertTrue(XMLoutput.indexOf("<item key=\"baunax\">\n            <value xsi:type=\"disconnectedFactHandle\"") > -1);

		cmd = getXStreamCommand();

		response = proxy.execute(cmd);

		assertEquals(200, response.getStatus());

		XMLoutput = (String) response.getEntity();

		ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML(XMLoutput);

		assertNotNull(result.getFactHandle("lucaz"));

		FlatQueryResults personsQuery = (FlatQueryResults) result.getValue("persons");
		assertEquals(2, personsQuery.size());
	}

}
