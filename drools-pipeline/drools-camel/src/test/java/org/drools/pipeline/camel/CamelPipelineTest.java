/*
 *  Copyright 2009 salaboy.
 * 
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
package org.drools.pipeline.camel;

import com.thoughtworks.xstream.XStream;
import java.util.Collection;
import junit.framework.TestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.SpringCamelContext;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.drools.FactHandle;
import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.common.InternalFactHandle;
import org.drools.definition.KnowledgePackage;
import org.drools.io.ResourceFactory;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.help.BatchExecutionHelper;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.impl.XStreamResolverStrategy;
import org.drools.vsm.ServiceManager;
import org.drools.vsm.local.ServiceManagerLocalClient;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author salaboy
 * @author lucaz
 */
public class CamelPipelineTest extends TestCase {

    CamelContext camel = null;
    private ClassPathXmlApplicationContext springCtx;

    public CamelPipelineTest(String testName) {
        super(testName);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected void setUp() throws Exception {
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setNormalize(true);
        springCtx = new ClassPathXmlApplicationContext("applicationContext.xml");
    }

    public void testCamelPipelineWithVSM() throws Exception {

        String inXml = "";
        inXml += "<batch-execution lookup=\"ksession1\" >";
        inXml += "  <insert out-identifier='salaboy'>";
        inXml += "    <org.drools.pipeline.camel.Person>";
        inXml += "      <name>salaboy</name>";
        inXml += "    </org.drools.pipeline.camel.Person>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        String rule = "";
        rule += "package org.drools.pipeline.camel;\n" +
                "import org.drools.pipeline.camel.Person;\n" +
                "rule 'Check for Person'\n" +
                " when\n" +
                "   $p: Person()\n" +
                " then\n" +
                "   System.out.println(\"Person Name: \" + $p.getName());\n" +
                "end\n";


        ServiceManager sm = new ServiceManagerLocalClient();

        StatefulKnowledgeSession ksession = getVmsSessionStateful(sm, rule);

        sm.register("ksession1",
                ksession);

        XStreamResolverStrategy xstreamStrategy = new XStreamResolverStrategy() {

            public XStream lookup(String name) {
                return BatchExecutionHelper.newXStreamMarshaller();
            }
        };

        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getCamelPipelineVms(sm,
                xstreamStrategy).insert(inXml,
                resultHandler);



        String outXml = (String) resultHandler.getObject();

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML(outXml);
        Person person = (Person) result.getValue("salaboy");
        assertEquals("salaboy",
                person.getName());

        FactHandle factHandle = (FactHandle) result.getFactHandle("salaboy");
        person = (Person) ksession.getObject(factHandle);
        assertEquals("salaboy",
                person.getName());

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier=\"salaboy\">\n";
        expectedXml += "    <org.drools.pipeline.camel.Person>\n";
        expectedXml += "      <name>salaboy</name>\n";
        expectedXml += "    </org.drools.pipeline.camel.Person>\n";
        expectedXml += "  </result>\n";
        expectedXml += "  <fact-handle identifier=\"salaboy\" externalForm=\"" + ((InternalFactHandle) result.getFactHandle("salaboy")).toExternalForm() + "\" /> \n";
        expectedXml += "</execution-results>\n";

        assertXMLEqual(expectedXml,
                outXml);




    }

    public void testCamelPipelineWithVSMAndSpring() throws Exception {

        String inXml = "";
        inXml += "<batch-execution lookup=\"ksession1\" >";
        inXml += "  <insert out-identifier='salaboy'>";
        inXml += "    <org.drools.pipeline.camel.Person>";
        inXml += "      <name>salaboy</name>";
        inXml += "    </org.drools.pipeline.camel.Person>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        String rule = "";
        rule += "package org.drools.pipeline.camel;\n" +
                "import org.drools.pipeline.camel.Person;\n" +
                "rule 'Check for Person'\n" +
                " when\n" +
                "   $p: Person()\n" +
                " then\n" +
                "   System.out.println(\"Person Name: \" + $p.getName());\n" +
                "end\n";

        ServiceManager sm = new ServiceManagerLocalClient();

        StatefulKnowledgeSession ksession = getVmsSessionStateful(sm, rule);

        sm.register("ksession1",
                ksession);


        ResultHandlerImpl resultHandler = new ResultHandlerImpl();

        getCamelPipelineVmsSpring(sm).insert(inXml, resultHandler);
        String outXml = (String) resultHandler.getObject();

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML(outXml);
        Person person = (Person) result.getValue("salaboy");
        assertEquals("salaboy",
                person.getName());

        FactHandle factHandle = (FactHandle) result.getFactHandle("salaboy");
        person = (Person) ksession.getObject(factHandle);
        assertEquals("salaboy",
                person.getName());

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier=\"salaboy\">\n";
        expectedXml += "    <org.drools.pipeline.camel.Person>\n";
        expectedXml += "      <name>salaboy</name>\n";
        expectedXml += "    </org.drools.pipeline.camel.Person>\n";
        expectedXml += "  </result>\n";
        expectedXml += "  <fact-handle identifier=\"salaboy\" externalForm=\"" + ((InternalFactHandle) result.getFactHandle("salaboy")).toExternalForm() + "\" /> \n";
        expectedXml += "</execution-results>\n";

        assertXMLEqual(expectedXml,
                outXml);


    }

    private StatefulKnowledgeSession getVmsSessionStateful(ServiceManager sm,
            String rule) throws Exception {
        KnowledgeBuilder kbuilder = sm.getKnowledgeBuilderFactory().newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(rule.getBytes()),
                ResourceType.DRL);

        if (kbuilder.hasErrors()) {
            System.out.println(kbuilder.getErrors());
        }

        assertFalse(kbuilder.hasErrors());
        Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();

        KnowledgeBase kbase = sm.getKnowledgeBaseFactory().newKnowledgeBase();

        kbase.addKnowledgePackages(pkgs);
        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();

        return session;
    }

    private Pipeline getCamelPipelineVms(ServiceManager vsm, XStreamResolverStrategy xstreamStrategy) throws Exception {

        Pipeline pipeline = new CamelServiceManagerPipelineImpl(vsm, new DefaultCamelContext());

        ((CamelServiceManagerPipelineImpl) pipeline).getCamelContext().addRoutes(new PipelineRouteBuilder(vsm, xstreamStrategy));

        ((CamelServiceManagerPipelineImpl) pipeline).startCamel();



        return pipeline;
    }

    private Pipeline getCamelPipelineVmsSpring(ServiceManager vsm) throws Exception {

        Pipeline pipeline = new CamelServiceManagerPipelineImpl(vsm, (SpringCamelContext) springCtx.getBean("camelContext"));

        ((CamelServiceManagerPipelineImpl) pipeline).startCamel();



        return pipeline;
    }

    public static class ResultHandlerImpl
            implements
            ResultHandler {

        Object object;

        public void handleResult(Object object) {
            this.object = object;
        }

        public Object getObject() {
            return this.object;
        }
    }

    private void assertXMLEqual(String expectedXml,
            String resultXml) {
        try {
            Diff diff = new Diff(expectedXml,
                    resultXml);
            diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
            XMLAssert.assertXMLEqual(diff,
                    true);
        } catch (Exception e) {
            throw new RuntimeException("XML Assertion failure",
                    e);
        }
    }
}

class PipelineRouteBuilder extends RouteBuilder {

    private XStreamResolverStrategy xstreamStrategy;

    public PipelineRouteBuilder(ServiceManager vsm, XStreamResolverStrategy xstreamStrategy) {

        this.xstreamStrategy = xstreamStrategy;


    }

    public void configure() {
        // START SNIPPET: example
        from("direct:start").process(new ToXmlNodeTransformer()).to("direct:xstreamTransformer");

        from("direct:xstreamTransformer").process(new CamelXStreamFromXmlVsmTransformer(xstreamStrategy)).to("direct:executor");
        from("direct:executor").process(new BatchExecutorProcessor()).to("direct:xstreamTransformerResult");

        from("direct:xstreamTransformerResult").process(new CamelXStreamToXmlVsmTransformer()).to("direct:finalResult");

        from("direct:finalResult").process(new AssignResultProcessor()).to("direct:executeResult");

        from("direct:executeResult").process(new ExecuteResultProcessor());

    }
}
