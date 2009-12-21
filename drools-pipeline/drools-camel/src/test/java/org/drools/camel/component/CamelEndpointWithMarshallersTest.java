/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.drools.camel.component;

import org.apache.camel.builder.RouteBuilder;
import org.drools.command.CommandFactory;
import org.drools.command.runtime.rule.GetObjectCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.common.DisconnectedFactHandle;
import org.drools.common.InternalFactHandle;
import org.drools.pipeline.camel.Person;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.help.BatchExecutionHelper;
import org.drools.runtime.rule.FactHandle;

public class CamelEndpointWithMarshallersTest extends DroolsCamelTestSupport {
    private String handle;

    public void testSimple(){
    }

//    public void testSessionInsert() throws Exception {
//
//        String cmd = "" +
//                "<batch-execution lookup=\"ksession1\">\n" +
//                    "<insert out-identifier=\"salaboy\">\n" +
//                        "<org.drools.pipeline.camel.Person>\n" +
//                            "<name>salaboy</name>\n" +
//                        "</org.drools.pipeline.camel.Person>\n" +
//                    "</insert>\n" +
//                    "<fire-all-rules/>\n" +
//                "</batch-execution>\n";
//
//        String outXml = new String((byte[])template.requestBody("direct:test-with-session", cmd));
//
//        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML(outXml);
//        Person person = (Person) result.getValue("salaboy");
//        assertEquals("salaboy",
//                person.getName());
//
//        String expectedXml = "<execution-results>\n";
//        expectedXml += "  <result identifier=\"salaboy\">\n";
//        expectedXml += "    <org.drools.pipeline.camel.Person>\n";
//        expectedXml += "      <name>salaboy</name>\n";
//        expectedXml += "    </org.drools.pipeline.camel.Person>\n";
//        expectedXml += "  </result>\n";
//        expectedXml += "  <fact-handle identifier=\"salaboy\" externalForm=\"" + ((InternalFactHandle) result.getFactHandle("salaboy")).toExternalForm() + "\"/>\n";
//        expectedXml += "</execution-results>";
//
//        assertEquals(expectedXml, outXml);
//
//    }
//
//    public void testNoSessionInsert() throws Exception {
//
//        String cmd = "" +
//                "<batch-execution lookup=\"ksession1\">\n" +
//                    "<insert out-identifier=\"salaboy\">\n" +
//                        "<org.drools.pipeline.camel.Person>\n" +
//                            "<name>salaboy</name>\n" +
//                        "</org.drools.pipeline.camel.Person>\n" +
//                    "</insert>\n" +
//                    "<fire-all-rules/>\n" +
//                "</batch-execution>\n";
//
//         String outXml = new String((byte[])template.requestBodyAndHeader("direct:test-no-session", cmd,
//            DroolsComponent.DROOLS_LOOKUP, "ksession1"));
//
//         ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML(outXml);
//        Person person = (Person) result.getValue("salaboy");
//        assertEquals("salaboy",
//                person.getName());
//
//        String expectedXml = "<execution-results>\n";
//        expectedXml += "  <result identifier=\"salaboy\">\n";
//        expectedXml += "    <org.drools.pipeline.camel.Person>\n";
//        expectedXml += "      <name>salaboy</name>\n";
//        expectedXml += "    </org.drools.pipeline.camel.Person>\n";
//        expectedXml += "  </result>\n";
//        expectedXml += "  <fact-handle identifier=\"salaboy\" externalForm=\"" + ((InternalFactHandle) result.getFactHandle("salaboy")).toExternalForm() + "\"/>\n";
//        expectedXml += "</execution-results>";
//
//        assertEquals(expectedXml, outXml);
//    }

//    public void testSessionGetObject() throws Exception {
//        FactHandle factHandle = new DisconnectedFactHandle(handle);
//        GetObjectCommand cmd = (GetObjectCommand) CommandFactory.newGetObject(factHandle);
//        cmd.setOutIdentifier("rider");
//
//        ExecutionResults response = (ExecutionResults) template.requestBody("direct:test-with-session", cmd);
//        assertTrue("Expected valid ExecutionResults object", response != null);
//        assertTrue("ExecutionResults missing expected object", response.getValue("rider") != null);
//        assertTrue("FactHandle object not of expected type", response.getValue("rider") instanceof Person);
//        assertEquals("Hadrian", ((Person)response.getValue("rider")).getName());
//    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("direct:test-with-session").to("drools://sm/ksession1?dataType=xstream");
                from("direct:test-no-session").to("drools://sm?pipeline=test-no-session&dataType=xstream");
            }
        };
    }

    @Override
    protected void configureDroolsContext() {
        Person me = new Person();
        me.setName("Hadrian");

        StatefulKnowledgeSession ksession = registerKnowledgeRuntime("ksession1", null);
        InsertObjectCommand cmd = new InsertObjectCommand(me);
        cmd.setOutIdentifier("camel-rider");
        cmd.setReturnObject(false);
        ExecutionResults results = ksession.execute(cmd);
        handle = ((FactHandle)results.getFactHandle("camel-rider")).toExternalForm();
    }
}
