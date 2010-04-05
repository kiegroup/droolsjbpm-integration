package org.drools.server;

import junit.framework.TestCase;

public abstract class KnowledgeServiceBaseTest extends TestCase {

	public String getJaxbCommand() throws Exception {
		String cmd = "";
		cmd += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
		cmd += "<batch-execution lookup='ksession1' xmlns:ns2='http://drools.org/model'>\n";
		cmd += "    <insert out-identifier='lucaz'>\n";
		cmd += "        <object xsi:type='ns2:person' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>\n";
		cmd += "            <ns2:name>lucaz</ns2:name>\n";
		cmd += "            <ns2:age>25</ns2:age>\n";
		cmd += "        </object>\n";
		cmd += "    </insert>\n";
		cmd += "    <insert out-identifier='baunax'>\n";
		cmd += "        <object xsi:type='ns2:person' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>\n";
		cmd += "            <ns2:name>baunax</ns2:name>\n";
		cmd += "            <ns2:age>21</ns2:age>\n";
		cmd += "        </object>\n";
		cmd += "    </insert>\n";
		cmd += "    <query name='persons' out-identifier='persons'/>\n";
		cmd += "    <fire-all-rules max='-1'/>\n";
		cmd += "</batch-execution>\n";
		return cmd;
	}
	
	public String getXStreamCommand() {
		String cmd = "";
		cmd += "<batch-execution lookup='ksession2'>\n";
		cmd += "   <insert out-identifier='baunax'>\n";
		cmd += "      <org.drools.pipeline.camel.Person>\n";
		cmd += "         <name>baunax</name>\n";
		cmd += "      </org.drools.pipeline.camel.Person>\n";
		cmd += "   </insert>\n";
		cmd += "   <insert out-identifier='lucaz'>\n";
		cmd += "      <org.drools.pipeline.camel.Person>\n";
		cmd += "         <name>lucaz</name>\n";
		cmd += "      </org.drools.pipeline.camel.Person>\n";
		cmd += "   </insert>\n";
		cmd += "   <query name='persons' out-identifier='persons'/>\n";
		cmd += "   <fire-all-rules/>\n";
		cmd += "</batch-execution>\n";
		return cmd;
	}
	
}
