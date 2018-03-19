/*
 * Copyright 2010 JBoss Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */

package org.kie.camel.embedded.camel.component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.DataFormatDefinition;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.common.InternalFactHandle;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.camel.embedded.component.KiePolicy;
import org.kie.internal.runtime.helper.BatchExecutionHelper;
import org.kie.pipeline.camel.Person;

public class CamelEndpointWithMarshallersTest extends KieCamelTestSupport {
    private String handle;

    @Test
    public void testSimple() {
    }

    @Test
    public void testSessionInsert() throws Exception {

        String cmd = "";
        cmd += "<batch-execution lookup=\"ksession1\">\n";
        cmd += "  <insert out-identifier=\"salaboy\">\n";
        cmd += "      <org.kie.camel.embedded.pipeline.camel.Person>\n";
        cmd += "         <name>salaboy</name>\n";
        cmd += "      </org.kie.camel.embedded.pipeline.camel.Person>\n";
        cmd += "   </insert>\n";
        cmd += "   <fire-all-rules/>\n";
        cmd += "</batch-execution>\n";

        String outXml = new String((byte[])template.requestBody("direct:test-with-session", cmd));

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML(outXml);
        Person person = (Person)result.getValue("salaboy");
        assertEquals("salaboy", person.getName());

        String expectedXml = "";
        expectedXml += "<?xml version='1.0' encoding='UTF-8'?><execution-results>";
        expectedXml += "<result identifier=\"salaboy\">";
        expectedXml += "<org.kie.camel.embedded.pipeline.camel.Person>";
        expectedXml += "<name>salaboy</name>";
        expectedXml += "</org.kie.camel.embedded.pipeline.camel.Person>";
        expectedXml += "</result>";
        expectedXml += "<fact-handle identifier=\"salaboy\" external-form=\"" + ((InternalFactHandle)result.getFactHandle("salaboy")).toExternalForm() + "\"/>";
        expectedXml += "</execution-results>";

        assertXMLEqual(expectedXml, outXml);

    }

    @Test
    public void testJSonSessionInsert() throws Exception {

        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "{\"insert\":{\"lookup\":\"ksession1\", ";
        inXml += "             \"object\":{\"org.kie.camel.embedded.pipeline.camel.Person\":{\"name\":\"salaboy\"}}, \"out-identifier\":\"salaboy\" } }";
        inXml += ", {\"fire-all-rules\":\"\"}";
        inXml += "]}}";

        String outXml = new String((byte[])template.requestBody("direct:test-with-session-json", inXml));

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML(outXml);
        Person person = (Person)result.getValue("salaboy");
        assertEquals("salaboy", person.getName());
    }

    @Test
    public void testNoSessionInsert() throws Exception {

        String cmd = "";
        cmd += "<batch-execution lookup=\"ksession1\">\n";
        cmd += "<insert out-identifier=\"salaboy\">\n";
        cmd += "<org.kie.camel.embedded.pipeline.camel.Person>\n";
        cmd += "<name>salaboy</name>\n";
        cmd += "</org.kie.camel.embedded.pipeline.camel.Person>\n";
        cmd += "</insert>\n";
        cmd += "<fire-all-rules/>\n";
        cmd += "</batch-execution>\n";

        String outXml = new String((byte[])template.requestBody("direct:test-no-session", cmd));

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML(outXml);
        Person person = (Person)result.getValue("salaboy");
        assertEquals("salaboy", person.getName());

        String expectedXml = "";
        expectedXml += "<?xml version='1.0' encoding='UTF-8'?><execution-results>";
        expectedXml += "<result identifier=\"salaboy\">";
        expectedXml += "<org.kie.camel.embedded.pipeline.camel.Person>";
        expectedXml += "<name>salaboy</name>";
        expectedXml += "</org.kie.camel.embedded.pipeline.camel.Person>";
        expectedXml += "</result>";
        expectedXml += "<fact-handle identifier=\"salaboy\" external-form=\"" + ((InternalFactHandle)result.getFactHandle("salaboy")).toExternalForm() + "\"/>";
        expectedXml += "</execution-results>";

        assertXMLEqual(expectedXml, outXml);
    }

    @Test
    public void testNoSessionInsertCustomXstream() throws Exception {

        String cmd = "";
        cmd += "<batch-execution lookup=\"ksession1\">\n";
        cmd += "<insert out-identifier=\"salaboy\">\n";
        cmd += "<org.kie.camel.embedded.pipeline.camel.Person name=\"salaboy\">\n";
        cmd += "</org.kie.camel.embedded.pipeline.camel.Person>\n";
        cmd += "</insert>\n";
        cmd += "<fire-all-rules/>\n";
        cmd += "</batch-execution>\n";

        String outXml = new String((byte[])template.requestBody("direct:test-no-session-custom", cmd));

        XStream xstream = BatchExecutionHelper.newXStreamMarshaller();
        PersonConverter converter = new PersonConverter();
        xstream.registerConverter(converter);

        ExecutionResults result = (ExecutionResults)xstream.fromXML(outXml);
        Person person = (Person)result.getValue("salaboy");
        assertEquals("salaboy", person.getName());

        String expectedXml = "";
        expectedXml += "<?xml version='1.0' encoding='UTF-8'?><execution-results>";
        expectedXml += "<result identifier=\"salaboy\">";
        expectedXml += "<org.kie.camel.embedded.pipeline.camel.Person name=\"salaboy\"/>";
        expectedXml += "</result>";
        expectedXml += "<fact-handle identifier=\"salaboy\" external-form=\"" + ((InternalFactHandle)result.getFactHandle("salaboy")).toExternalForm() + "\"/>";
        expectedXml += "</execution-results>";

        assertXMLEqual(expectedXml, outXml);
    }

    @Test
    public void testSessionGetObject() throws Exception {
        String cmd = "";
        cmd += "<batch-execution lookup=\"ksession1\">\n";
        cmd += "<get-object out-identifier=\"rider\" fact-handle=\"" + this.handle + "\"/>\n";
        cmd += "</batch-execution>\n";

        String outXml = new String((byte[])template.requestBody("direct:test-with-session", cmd));

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML(outXml);
        Person person = (Person)result.getValue("rider");
        assertEquals("Hadrian", person.getName());

        String expectedXml = "";
        expectedXml += "<?xml version='1.0' encoding='UTF-8'?><execution-results>";
        expectedXml += "<result identifier=\"rider\">";
        expectedXml += "<org.kie.camel.embedded.pipeline.camel.Person>";
        expectedXml += "<name>Hadrian</name>";
        expectedXml += "</org.kie.camel.embedded.pipeline.camel.Person>";
        expectedXml += "</result>";
        expectedXml += "</execution-results>";

        assertXMLEqual(expectedXml, outXml);

    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                org.apache.camel.model.dataformat.XStreamDataFormat xstreamDataFormat = new org.apache.camel.model.dataformat.XStreamDataFormat();
                xstreamDataFormat.setConverters(Arrays.asList(new String[] {PersonConverter.class.getName()}));

                Map<String, DataFormatDefinition> dataFormats = new HashMap<String, DataFormatDefinition>();
                dataFormats.put("custom-xstream", xstreamDataFormat);
                getContext().setDataFormats(dataFormats);

                from("direct:test-with-session").policy(new KiePolicy()).unmarshal("xstream").to("kie:ksession1").marshal("xstream");
                from("direct:test-with-session-json").policy(new KiePolicy()).unmarshal("json").to("kie:ksession1").marshal("json");
                from("direct:test-no-session").policy(new KiePolicy()).unmarshal("xstream").to("kie:dynamic").marshal("xstream");
                from("direct:test-no-session-custom").policy(new KiePolicy()).unmarshal("custom-xstream").to("kie:dynamic").marshal("custom-xstream");
            }
        };
    }

    @Override
    protected void configureDroolsContext(javax.naming.Context jndiContext) {
        Person me = new Person();
        me.setName("Hadrian");

        KieSession ksession = registerKnowledgeRuntime("ksession1", null);
        InsertObjectCommand cmd = new InsertObjectCommand(me);
        cmd.setOutIdentifier("camel-rider");
        cmd.setReturnObject(false);

        BatchExecutionCommandImpl script = new BatchExecutionCommandImpl(Arrays.asList(new Command<?>[] {cmd}));

        ExecutionResults results = ksession.execute(script);
        handle = ((FactHandle)results.getFactHandle("camel-rider")).toExternalForm();

    }

    public static class PersonConverter implements Converter {

        public PersonConverter() {
        }

        public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
            Person p = (Person)object;
            writer.addAttribute("name", p.getName());
        }

        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            Person p = new Person(reader.getAttribute("name"));
            return p;
        }

        public boolean canConvert(Class clazz) {
            return clazz.equals(Person.class);
        }
    }
}
