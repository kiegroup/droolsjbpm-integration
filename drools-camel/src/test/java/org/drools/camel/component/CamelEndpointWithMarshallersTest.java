/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.DataFormatDefinition;
import org.drools.command.impl.GenericCommand;
import org.drools.command.runtime.BatchExecutionCommandImpl;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.common.InternalFactHandle;
import org.drools.pipeline.camel.Person;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.help.BatchExecutionHelper;
import org.drools.runtime.rule.FactHandle;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class CamelEndpointWithMarshallersTest extends DroolsCamelTestSupport {
    private String handle;

    public void testSimple() {
    }

    public void testSessionInsert() throws Exception {

        String cmd = "";
        cmd += "<batch-execution lookup=\"ksession1\">\n";
        cmd += "  <insert out-identifier=\"salaboy\">\n";
        cmd += "      <org.drools.pipeline.camel.Person>\n";
        cmd += "         <name>salaboy</name>\n";
        cmd += "      </org.drools.pipeline.camel.Person>\n";
        cmd += "   </insert>\n";
        cmd += "   <fire-all-rules/>\n";
        cmd += "</batch-execution>\n";

        String outXml = new String( (byte[]) template.requestBody( "direct:test-with-session",
                                                                   cmd ) );

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
        Person person = (Person) result.getValue( "salaboy" );
        assertEquals( "salaboy",
                      person.getName() );

        String expectedXml = "";
        expectedXml += "<?xml version='1.0' encoding='UTF-8'?><execution-results>";
        expectedXml += "<result identifier=\"salaboy\">";
        expectedXml += "<org.drools.pipeline.camel.Person>";
        expectedXml += "<name>salaboy</name>";
        expectedXml += "</org.drools.pipeline.camel.Person>";
        expectedXml += "</result>";
        expectedXml += "<fact-handle identifier=\"salaboy\" external-form=\"" + ((InternalFactHandle) result.getFactHandle( "salaboy" )).toExternalForm() + "\"/>";
        expectedXml += "</execution-results>";

        assertXMLEqual( expectedXml,
                        outXml );

    }

    public void testJSonSessionInsert() throws Exception {

        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "{\"insert\":{\"lookup\":\"ksession1\", ";
        inXml += "             \"object\":{\"org.drools.pipeline.camel.Person\":{\"name\":\"salaboy\"}}, \"out-identifier\":\"salaboy\" } }";
        inXml += ", {\"fire-all-rules\":\"\"}";
        inXml += "]}}";

        String outXml = new String( (byte[]) template.requestBody( "direct:test-with-session-json",
                                                                   inXml ) );

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        Person person = (Person) result.getValue( "salaboy" );
        assertEquals( "salaboy",
                      person.getName() );
    }

    public void testNoSessionInsert() throws Exception {

        String cmd = "";
        cmd += "<batch-execution lookup=\"ksession1\">\n";
        cmd += "<insert out-identifier=\"salaboy\">\n";
        cmd += "<org.drools.pipeline.camel.Person>\n";
        cmd += "<name>salaboy</name>\n";
        cmd += "</org.drools.pipeline.camel.Person>\n";
        cmd += "</insert>\n";
        cmd += "<fire-all-rules/>\n";
        cmd += "</batch-execution>\n";

        String outXml = new String( (byte[]) template.requestBody( "direct:test-no-session",
                                                                   cmd ) );

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
        Person person = (Person) result.getValue( "salaboy" );
        assertEquals( "salaboy",
                      person.getName() );

        String expectedXml = "";
        expectedXml += "<?xml version='1.0' encoding='UTF-8'?><execution-results>";
        expectedXml += "<result identifier=\"salaboy\">";
        expectedXml += "<org.drools.pipeline.camel.Person>";
        expectedXml += "<name>salaboy</name>";
        expectedXml += "</org.drools.pipeline.camel.Person>";
        expectedXml += "</result>";
        expectedXml += "<fact-handle identifier=\"salaboy\" external-form=\"" + ((InternalFactHandle) result.getFactHandle( "salaboy" )).toExternalForm() + "\"/>";
        expectedXml += "</execution-results>";

        assertXMLEqual( expectedXml,
                        outXml );
    }

    public void testNoSessionInsertCustomXstream() throws Exception {

        String cmd = "";
        cmd += "<batch-execution lookup=\"ksession1\">\n";
        cmd += "<insert out-identifier=\"salaboy\">\n";
        cmd += "<org.drools.pipeline.camel.Person name=\"salaboy\">\n";
        cmd += "</org.drools.pipeline.camel.Person>\n";
        cmd += "</insert>\n";
        cmd += "<fire-all-rules/>\n";
        cmd += "</batch-execution>\n";

        String outXml = new String( (byte[]) template.requestBody( "direct:test-no-session-custom",
                                                                   cmd ) );

        XStream xstream = BatchExecutionHelper.newXStreamMarshaller();
        PersonConverter converter = new PersonConverter();
        xstream.registerConverter( converter );

        ExecutionResults result = (ExecutionResults) xstream.fromXML( outXml );
        Person person = (Person) result.getValue( "salaboy" );
        assertEquals( "salaboy",
                      person.getName() );

        String expectedXml = "";
        expectedXml += "<?xml version='1.0' encoding='UTF-8'?><execution-results>";
        expectedXml += "<result identifier=\"salaboy\">";
        expectedXml += "<org.drools.pipeline.camel.Person name=\"salaboy\"/>";
        expectedXml += "</result>";
        expectedXml += "<fact-handle identifier=\"salaboy\" external-form=\"" + ((InternalFactHandle) result.getFactHandle( "salaboy" )).toExternalForm() + "\"/>";
        expectedXml += "</execution-results>";

        assertXMLEqual( expectedXml,
                        outXml );
    }

    public void testSessionGetObject() throws Exception {
        String cmd = "";
        cmd += "<batch-execution lookup=\"ksession1\">\n";
        cmd += "<get-object out-identifier=\"rider\" fact-handle=\"" + this.handle + "\"/>\n";
        cmd += "</batch-execution>\n";

        String outXml = new String( (byte[]) template.requestBody( "direct:test-with-session",
                                                                   cmd ) );

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
        Person person = (Person) result.getValue( "rider" );
        assertEquals( "Hadrian",
                      person.getName() );

        String expectedXml = "";
        expectedXml += "<?xml version='1.0' encoding='UTF-8'?><execution-results>";
        expectedXml += "<result identifier=\"rider\">";
        expectedXml += "<org.drools.pipeline.camel.Person>";
        expectedXml += "<name>Hadrian</name>";
        expectedXml += "</org.drools.pipeline.camel.Person>";
        expectedXml += "</result>";
        expectedXml += "</execution-results>";

        assertXMLEqual( expectedXml,
                        outXml );

    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                org.apache.camel.model.dataformat.XStreamDataFormat xstreamDataFormat = new org.apache.camel.model.dataformat.XStreamDataFormat();
                xstreamDataFormat.setConverters( Arrays.asList( new String[]{PersonConverter.class.getName()} ) );

                Map<String, DataFormatDefinition> dataFormats = new HashMap<String, DataFormatDefinition>();
                dataFormats.put( "custom-xstream",
                                 xstreamDataFormat );
                getContext().setDataFormats( dataFormats );

                from( "direct:test-with-session" ).policy( new DroolsPolicy() ).unmarshal( "xstream" ).to( "drools:node/ksession1" ).marshal( "xstream" );
                from( "direct:test-with-session-json" ).policy( new DroolsPolicy() ).unmarshal( "json" ).to( "drools:node/ksession1" ).marshal( "json" );
                from( "direct:test-no-session" ).policy( new DroolsPolicy() ).unmarshal( "xstream" ).to( "drools:node" ).marshal( "xstream" );
                from( "direct:test-no-session-custom" ).policy( new DroolsPolicy() ).unmarshal( "custom-xstream" ).to( "drools:node" ).marshal( "custom-xstream" );
            }
        };
    }

    @Override
    protected void configureDroolsContext(javax.naming.Context jndiContext) {
        Person me = new Person();
        me.setName( "Hadrian" );

        StatefulKnowledgeSession ksession = registerKnowledgeRuntime( "ksession1",
                                                                      null );
        InsertObjectCommand cmd = new InsertObjectCommand( me );
        cmd.setOutIdentifier( "camel-rider" );
        cmd.setReturnObject( false );

        BatchExecutionCommandImpl script = new BatchExecutionCommandImpl( Arrays.asList( new GenericCommand< ? >[]{cmd} ) );

        ExecutionResults results = ksession.execute( script );
        handle = ((FactHandle) results.getFactHandle( "camel-rider" )).toExternalForm();

    }

    public static class PersonConverter
        implements
        Converter {

        public PersonConverter() {
        }

        public void marshal(Object object,
                            HierarchicalStreamWriter writer,
                            MarshallingContext context) {
            Person p = (Person) object;
            writer.addAttribute( "name",
                                 p.getName() );
        }

        public Object unmarshal(HierarchicalStreamReader reader,
                                UnmarshallingContext context) {
            Person p = new Person( reader.getAttribute( "name" ) );
            return p;
        }

        public boolean canConvert(Class clazz) {
            return clazz.equals( Person.class );
        }
    }
}
