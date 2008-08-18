package org.drools.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.drools.Person;
import org.drools.RuleBase;
import org.drools.StatefulSession;
import org.drools.StatelessSession;
import org.drools.common.InternalRuleBase;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

public class RuleService extends HttpServlet {


	static XStream xmlInstance = configureXStream(false);
	static XStream jsonInstance = configureXStream(true);


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse resp)
			throws ServletException, IOException {

		String uri = request.getRequestURI();
		RuleBase rb =  getRuleBaseFromURI(uri);

		//doService(request.getInputStream(), resp.getOutputStream(), rb);


		//sample();


	}

	void doService(InputStream inputStream,
			OutputStream outputStream, RuleBase rb, boolean json) {
		InternalRuleBase irb = (InternalRuleBase) rb;
		ClassLoader originalCL = Thread.currentThread().getContextClassLoader();
		try {
			ClassLoader cl = irb.getRootClassLoader();
			Thread.currentThread().setContextClassLoader(cl);

			XStream xs = (json) ? jsonInstance : xmlInstance;
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

	private RuleBase getRuleBaseFromURI(String uri) {

		return null;
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

	public static void main(String[] args) throws ServletException, IOException {
		RuleService rs = new RuleService();
		rs.sample();

	}


}
