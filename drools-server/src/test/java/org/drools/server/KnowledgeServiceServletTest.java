package org.drools.server;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.compiler.PackageBuilder;
import org.drools.server.KnowledgeStatelessServlet;

import junit.framework.TestCase;

public class KnowledgeServiceServletTest extends TestCase {


	public void testSample() throws Exception {

		PackageBuilder pb = new PackageBuilder();
		pb.addPackageFromDrl(new InputStreamReader(getClass().getResourceAsStream("test_rules1.drl")));
		assertFalse(pb.hasErrors());

		RuleBase rb = RuleBaseFactory.newRuleBase();
		rb.addPackage(pb.getPackage());

		KnowledgeStatelessServlet serv = new KnowledgeStatelessServlet();

		final InputStream inXML = getClass().getResourceAsStream("sample_request.xml");
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();


		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		serv.doService(inXML, outStream, rb, false);

		String result = new String(outStream.toByteArray());

		assertTrue(result.indexOf("<id>prs</id>") > -1);
		assertTrue(result.indexOf("<id>result</id>") > -1);
		assertTrue(result.indexOf("<name>Jo</name>") > -1);
		assertTrue(result.indexOf("<status>crazy</status>") > -1);
		assertTrue(result.indexOf("<value>42</value>") > -1);
		assertTrue(result.indexOf("<explanation>just cause it is</explanation>") > -1);

		assertSame(cl, Thread.currentThread().getContextClassLoader());


		InputStream inJSON = getClass().getResourceAsStream("sample_request.json");
		outStream = new ByteArrayOutputStream();
		serv.doService(inJSON, outStream, rb, true);

		result = new String(outStream.toByteArray());

		assertTrue(result.indexOf("\"id\":\"prs\"") > -1);
		assertTrue(result.indexOf("\"id\":\"result\"") > -1);
		assertTrue(result.indexOf("\"status\":\"crazy\"") > -1);


	}

	public void testInitialise() {
		KnowledgeStatelessServlet rs = new KnowledgeStatelessServlet();
		assertNotNull(rs.jsonInstance);
		assertNotNull(rs.xmlInstance);

	}

	public void testURLPattern() {
		Matcher m = KnowledgeStatelessServlet.urlPattern.matcher("foo/knowledgebase/whee");
		assertTrue(m.matches());
		assertEquals("whee", m.group(1));
		m = KnowledgeStatelessServlet.urlPattern.matcher("foo/knowledgebase");
		assertFalse(m.matches());
	}


}
