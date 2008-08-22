package org.drools.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.drools.Person;
import org.drools.RuleBase;
import org.drools.StatelessSession;
import org.drools.agent.RuleAgent;
import org.drools.common.InternalRuleBase;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

/**
 * This provides a stateless service for dealing with rules/knowledge bases.
 *
 * Supported operations are:
 *
 *  POST: this will execute the service and return the results.
 *
 *  Content-Type: by default XML will be assumed, if application/json is used, then JSON will be used instead.
 *
 *  URIs:
 *
 *  http://server/drools-server/knowledgebase/{configName}
 *
 *  {configName} is the name of the rule agent configuration to load - this is generally a properties file located in the root of the classpath
 *  for this app.
 *
 * @author Michael Neale
 */
public class KnowledgeStatelessServlet extends HttpServlet {


	private static final long serialVersionUID = -8239975288596976901L;
	static Map<String, RuleAgent> cachedAgents = new HashMap<String, RuleAgent>();
	static Pattern urlPattern = Pattern.compile(".*knowledgebase/(.*)");

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse resp)
			throws ServletException, IOException {
		String uri = request.getRequestURI();
		String contentType = request.getContentType();
		resp.setContentType(contentType);
		Matcher m = urlPattern.matcher(uri);
		if (!m.matches()) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "URI needs to be of the form /knowledgebase/{configName}");
			return;
		}
		String conf = m.group(1);
		RuleBase rb =  getRuleBase(conf);
		if (rb == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The rulebase [" + conf + "] was not found or not valid.");
			return;
		}
		try {
			if (contentType != null && (contentType.indexOf("json") > -1)) {
				//do json version
				doService(getInputStream(request), getOutputStream(resp), rb, true);
			} else {
				//xml version
				doService(getInputStream(request), getOutputStream(resp), rb, false);
			}
		} catch (ConversionException e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Unable to convert. Error: " + e.getMessage() );
		}
	}


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.sendRedirect("");
	}



	RuleBase getRuleBase(String configName) {
		if (cachedAgents.containsKey(configName)) {
			return cachedAgents.get(configName).getRuleBase();
		} else {
			synchronized (cachedAgents) {
				if (!cachedAgents.containsKey(configName)) {
					 try {
						RuleAgent ag = RuleAgent.newRuleAgent("/" + configName + ".properties");
						cachedAgents.put(configName, ag);
					 } catch (NullPointerException npe) {
						 npe.printStackTrace();
						 return null;
					 }
				}
				return cachedAgents.get(configName).getRuleBase();
			}
		}
	}




	void doService(InputStream inputStream,
			OutputStream outputStream, RuleBase rb, boolean json) {
		InternalRuleBase irb = (InternalRuleBase) rb;
		ClassLoader originalCL = Thread.currentThread().getContextClassLoader();
		try {
			ClassLoader cl = irb.getRootClassLoader();
			Thread.currentThread().setContextClassLoader(cl);

			XStream xs = configureXStream(json);
			ServiceRequestMessage req = (ServiceRequestMessage) xs.fromXML(inputStream);
			StatelessSession session  = rb.newStatelessSession();
			if (req.globals != null) {
				for (NamedFact nf : req.globals) {
					session.setGlobal(nf.id, nf.fact);
				}
			}
			List<Object> facts = new ArrayList<Object>();
			if (req.inFacts != null) {
				for (AnonFact f : req.inFacts) {
					facts.add(f.fact);
				}
			}
			if (req.inOutFacts != null) {
				for (NamedFact nf : req.inOutFacts) {
					facts.add(nf.fact);
				}
			}

			session.execute(facts);

			ServiceResponseMessage res = new ServiceResponseMessage();
			if (req.globals != null) {
				res.globals = req.globals;
			}
			if (req.inOutFacts != null) {
				res.inOutFacts = req.inOutFacts;
			}

			xs.toXML(res, outputStream);


		} finally {

			Thread.currentThread().setContextClassLoader(originalCL);
		}
	}


	static XStream configureXStream(boolean json) {
		if (json) {
			XStream xs = new XStream(new JettisonMappedXmlDriver());
			alias(xs);
			return xs;

		} else {
			XStream xs = new XStream();
			alias(xs);
			return xs;
		}
	}

	private static void alias(XStream xs) {
		xs.alias("knowledgebase-request", ServiceRequestMessage.class);
		xs.alias("knowledgebase-response", ServiceResponseMessage.class);
		xs.alias("named-fact", NamedFact.class);
		xs.alias("anon-fact", AnonFact.class);
	}




	OutputStream getOutputStream(HttpServletResponse resp) throws IOException {
		return resp.getOutputStream();
	}

	InputStream getInputStream(HttpServletRequest request) throws IOException {
		return request.getInputStream();
	}




	private void sample() {
		XStream xs = configureXStream(true);

		ServiceRequestMessage req = new ServiceRequestMessage();

		req.globals = new NamedFact[1];
		req.inFacts = new AnonFact[1];
		req.inOutFacts = new NamedFact[1];
		req.globals[0] = new NamedFact("jo", new Person("Jo", "Chocolote"));
		//req.inFacts[0] = new AnonFact(new Person("Mike", "beer"));
		req.inFacts[0] = new AnonFact(new Person("Mike", "wine"));
		req.inOutFacts[0] = new NamedFact("mark", new Person("Mark", "Cheese"));

		String requestMessage = xs.toXML(req);

		ServiceRequestMessage req_ = (ServiceRequestMessage) xs.fromXML(requestMessage);
		String requestMessage_ = xs.toXML(req_);

		System.out.println(requestMessage);

		if (!requestMessage_.equals(requestMessage)) throw new RuntimeException("fail !");
	}



	public static void main(String[] args) throws ServletException, IOException {
		KnowledgeStatelessServlet rs = new KnowledgeStatelessServlet();
		rs.sample();

	}

	public static String getRequestExample(boolean json) {
		ServiceRequestMessage req = new ServiceRequestMessage();
		req.globals = new NamedFact[1];
		req.globals[0] = new NamedFact("myglobal", new ExampleFact("Saab", 42));
		req.inFacts = new AnonFact[2];
		req.inFacts[0] = new AnonFact(new ExampleFact("Audi", 55));
		req.inFacts[1] = new AnonFact(new ExampleFact("Mercedes", 65));
		req.inOutFacts = new NamedFact[1];
		req.inOutFacts[0] = new NamedFact("myfact", new ExampleFact("BMW", 50));
		return configureXStream(json).toXML(req);
	}


	public static String getResponseExample(boolean json) {
		ServiceResponseMessage req = new ServiceResponseMessage();
		req.globals = new NamedFact[1];
		req.globals[0] = new NamedFact("myglobal", new ExampleFact("Saab", 42));
		req.inOutFacts = new NamedFact[1];
		req.inOutFacts[0] = new NamedFact("myfact", new ExampleFact("BMW", 50));
		return configureXStream(json).toXML(req);
	}


}
