package org.drools.server;

import java.io.ByteArrayInputStream;
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


public class QueryKnowledgeServiceServletTest extends TestCase {

	public void testEndToEndIntegration() throws Exception {
		File source = new File("TestService2.drl");
		if (source.exists())  {
			System.err.println("Deleting !");
			source.delete();
		}
		try {
			InputStream original = getClass().getResourceAsStream("test_query_rules1.drl");
			copy(original, source);
			Thread.sleep(120);
			StubbedServlet serv = new StubbedServlet();
			final InputStream inXML = getClass().getResourceAsStream("sample_query_request.xml");
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			serv.inputStream = inXML;
			serv.outputStream = outStream;

			MockHTTPResponse resp = new MockHTTPResponse();
			serv.doPost(new MockHttpRequest("something/knowledgebase/testservice2", "application/xml"), resp);

			String resultXML = new String(outStream.toByteArray());
			System.out.println("\nResult:\n" + resultXML);

			assertTrue(resultXML.indexOf("<value>42</value>") > -1);
			assertEquals("application/xml", resp.contentType);

		} finally {
			source.delete();
		}
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

}
