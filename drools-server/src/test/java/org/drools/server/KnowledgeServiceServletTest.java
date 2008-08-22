package org.drools.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.common.InternalRuleBase;
import org.drools.compiler.PackageBuilder;

import com.thoughtworks.xstream.XStream;

public class KnowledgeServiceServletTest extends TestCase {


	public void testEndToEndIntegration() throws Exception {
		File source = new File("TestService.drl");
		if (source.exists()) source.delete();
		try {
			InputStream original = getClass().getResourceAsStream("test_rules1.drl");
			copy(original, source);
			Thread.sleep(120);
			StubbedServlet serv = new StubbedServlet();
			final InputStream inXML = getClass().getResourceAsStream("sample_request.xml");
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			serv.inputStream = inXML;
			serv.outputStream = outStream;

			MockHTTPResponse resp = new MockHTTPResponse();
			serv.doPost(new MockHttpRequest("something/knowledgebase/testservice", "application/xml"), resp);

			String resultXML = new String(outStream.toByteArray());
			assertTrue(resultXML.indexOf("<value>42</value>") > -1);
			assertEquals("application/xml", resp.contentType);


			resp = new MockHTTPResponse();
			serv.inputStream = getClass().getResourceAsStream("sample_request.json");
			outStream = new ByteArrayOutputStream();
			serv.outputStream = outStream;
			serv.doPost(new MockHttpRequest("something/knowledgebase/testservice", "application/json"), resp);

			String resultJSON = new String(outStream.toByteArray());
			assertNotNull(resultJSON);
			assertEquals("application/json", resp.contentType);
			assertTrue(resultJSON.indexOf("\"status\":\"crazy\"") > -1);

			resp = new MockHTTPResponse();
			serv.inputStream = getClass().getResourceAsStream("sample_request.json");
			outStream = new ByteArrayOutputStream();
			serv.outputStream = outStream;
			serv.doPost(new MockHttpRequest("something/foox", "application/json"), resp);
			assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.errorCode);

			resp = new MockHTTPResponse();
			serv.doGet(new MockHttpRequest(null, null), resp);
			assertNotNull(resp.redirect);
			//assertTrue(resp.redirect.endsWith("index.jsp"));


			//check bad
			serv.doPost(new MockHttpRequest("something/knowledgebase/XXX", "application/json"), resp);
			assertEquals(HttpServletResponse.SC_NOT_FOUND, resp.errorCode);


			//check bad request
			resp = new MockHTTPResponse();
			serv.inputStream = getClass().getResourceAsStream("sample_request_bad.json");
			outStream = new ByteArrayOutputStream();
			serv.outputStream = outStream;
			serv.doPost(new MockHttpRequest("something/knowledgebase/testservice", "application/json"), resp);
			assertEquals(HttpServletResponse.SC_NOT_ACCEPTABLE, resp.errorCode);


		} finally {
			source.delete();
		}
	}

	public void testSample() throws Exception {

		PackageBuilder pb = new PackageBuilder();
		pb.addPackageFromDrl(new InputStreamReader(getClass().getResourceAsStream("test_rules1.drl")));
		assertFalse(pb.hasErrors());

		RuleBase rb = RuleBaseFactory.newRuleBase();
		rb.addPackage(pb.getPackage());

		KnowledgeStatelessServlet serv = new KnowledgeStatelessServlet();


		//serv.xmlInstance = KnowledgeStatelessServlet.configureXStream(false);

		final InputStream inXML = getClass().getResourceAsStream("sample_request.xml");
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();


		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		serv.doService(inXML, outStream, rb, false);



		String result = new String(outStream.toByteArray());


		assertTrue(result.indexOf("<id>prs</id>") > -1);
		assertTrue(result.indexOf("<id>result</id>") > -1);
		assertTrue(result.indexOf("<name>Jo</name>") > -1);
		assertTrue(result, result.indexOf("<status>crazy</status>") > -1);
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


	public void testURLPattern() {
		Matcher m = KnowledgeStatelessServlet.urlPattern.matcher("foo/knowledgebase/whee");
		assertTrue(m.matches());
		assertEquals("whee", m.group(1));
		m = KnowledgeStatelessServlet.urlPattern.matcher("foo/knowledgebase");
		assertFalse(m.matches());
	}

	public void testXmlVsJSON() throws Exception {
		PackageBuilder pb = new PackageBuilder();
		pb.addPackageFromDrl(new InputStreamReader(getClass().getResourceAsStream("test_rules1.drl")));
		assertFalse(pb.hasErrors());

		RuleBase rb = RuleBaseFactory.newRuleBase();
		rb.addPackage(pb.getPackage());

		Thread.currentThread().setContextClassLoader(((InternalRuleBase) rb).getRootClassLoader());

		long time = System.currentTimeMillis();
		for (int i = 0; i < 99; i++) {
			InputStream inXML = getClass().getResourceAsStream("sample_request.xml");
			XStream xs = KnowledgeStatelessServlet.configureXStream(false);
			xs.fromXML(inXML);
		}
		System.out.println("XML time:" + (System.currentTimeMillis() - time));

		time = System.currentTimeMillis();
		for (int i = 0; i < 99; i++) {
			InputStream inXML = getClass().getResourceAsStream("sample_request.json");
			XStream xs = KnowledgeStatelessServlet.configureXStream(true);
			xs.fromXML(inXML);
		}
		System.out.println("JSON time:" + (System.currentTimeMillis() - time));

//		time = System.currentTimeMillis();
//		for (int i = 0; i < 99; i++) {
//			InputStream inXML = getClass().getResourceAsStream("sample_request_large.xml");
//			XStream xs = KnowledgeStatelessServlet.configureXStream(false);
//			xs.fromXML(inXML);
//		}
//		System.out.println("LARGE XML time:" + (System.currentTimeMillis() - time));

	}



	public void testExample() {
		String xml = KnowledgeStatelessServlet.getRequestExample(false);
		assertNotNull(xml);
		String json = KnowledgeStatelessServlet.getRequestExample(true);
		assertNotNull(json);
		assertFalse(json.equals(xml));


		xml = KnowledgeStatelessServlet.getResponseExample(false);
		assertNotNull(xml);
		json = KnowledgeStatelessServlet.getResponseExample(true);
		assertNotNull(json);
		assertFalse(json.equals(xml));

	}



    void copy(InputStream in, File dst) throws IOException {
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

	public void testInitTiming() {
		long time = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			XStream xs = KnowledgeStatelessServlet.configureXStream(false);
		}
		System.out.println("Timing: " + (System.currentTimeMillis() - time));
	}




}
