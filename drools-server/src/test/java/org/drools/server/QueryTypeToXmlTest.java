package org.drools.server;

import junit.framework.TestCase;

import com.thoughtworks.xstream.XStream;

public class QueryTypeToXmlTest extends TestCase {
	
	public void testSimpleQueryType() throws Exception {
		
		String[] factNames = {"fact1", "fact2" };
		Object[] args = { new Integer(1), new Integer(2) };
		
		QueryType qt = new QueryType("MyQuery", factNames, args);
		
		XStream xstream = new XStream();
		xstream.alias("query-type", QueryType.class);
		
        System.out.println(xstream.toXML(qt));
	}
	
	public void testEmptyQueryType() throws Exception {
		
		QueryType qt = new QueryType("MyEmptyQuery", null, null);

		XStream xstream = new XStream();
		xstream.alias("query-type", QueryType.class);
		
        System.out.println(xstream.toXML(qt));
		
	}

}
