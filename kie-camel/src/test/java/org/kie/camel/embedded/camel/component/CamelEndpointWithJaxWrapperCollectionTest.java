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

package org.kie.camel.embedded.camel.component;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.GetGlobalCommand;
import org.drools.core.command.runtime.SetGlobalCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.command.Command;
import org.kie.api.io.KieResources;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.camel.embedded.component.KiePolicy;
import org.kie.internal.builder.JaxbConfiguration;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.pipeline.camel.Person;
import org.kie.pipeline.camel.WrappedList;

public class CamelEndpointWithJaxWrapperCollectionTest extends KieCamelTestSupport {

    private String handle;

    @Test
    public void testWorkingSetGlobalTestSessionSetAndGetGlobal() throws Exception {

        BatchExecutionCommandImpl cmd = new BatchExecutionCommandImpl();
        cmd.setLookup("ksession1");

        SetGlobalCommand setGlobal = new SetGlobalCommand("list", new WrappedList());
        setGlobal.setOutIdentifier("list");

        cmd.getCommands().add(setGlobal);
        cmd.getCommands().add(new InsertObjectCommand(new Person("baunax")));
        cmd.getCommands().add(new FireAllRulesCommand());
        cmd.getCommands().add(new GetGlobalCommand("list"));

        Marshaller marshaller = getJaxbContext().createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        StringWriter xml = new StringWriter();
        marshaller.marshal(cmd, xml);

        logger.debug(xml.toString());

        byte[] response = (byte[])template.requestBody("direct:test-with-session", xml.toString());
        assertNotNull(response);
        logger.debug("response:\n" + new String(response));
        Unmarshaller unmarshaller = getJaxbContext().createUnmarshaller();
        ExecutionResults res = (ExecutionResults)unmarshaller.unmarshal(new ByteArrayInputStream(response));
        WrappedList resp = (WrappedList)res.getValue("list");
        assertNotNull(resp);

        assertEquals(resp.size(), 2);
        assertEquals("baunax", resp.get(0).getName());
        assertEquals("Hadrian", resp.get(1).getName());

    }

    @Override
    protected void configureDroolsContext(Context jndiContext) {
        Person me = new Person();
        me.setName("Hadrian");

        String rule = "";
        rule += "package org.kie.pipeline.camel \n";
        rule += "import org.kie.camel.embedded.pipeline.camel.Person\n";
        rule += "import org.kie.camel.embedded.pipeline.camel.WrappedList\n";
        rule += "global WrappedList list\n";
        rule += "rule rule1 \n";
        rule += "  when \n";
        rule += "    $p : Person() \n";
        rule += " \n";
        rule += "  then \n";
        rule += "    System.out.println(\"executed\"); \n";
        rule += "    list.add($p); \n";
        rule += "end\n";

        KieSession ksession = registerKnowledgeRuntime("ksession1", rule);
        InsertObjectCommand cmd = new InsertObjectCommand(me);
        cmd.setOutIdentifier("camel-rider");
        cmd.setReturnObject(false);
        BatchExecutionCommandImpl script = new BatchExecutionCommandImpl(Arrays.asList(new Command<?>[] {cmd}));

        ExecutionResults results = ksession.execute(script);
        handle = ((FactHandle)results.getFactHandle("camel-rider")).toExternalForm();
    }

    @Override
    protected KieSession registerKnowledgeRuntime(String identifier, String rule) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        KieResources kieResources = ks.getResources();

        Options xjcOpts = new Options();
        xjcOpts.setSchemaLanguage(Language.XMLSCHEMA);

        JaxbConfiguration jaxbConfiguration = KnowledgeBuilderFactory.newJaxbConfiguration(xjcOpts, "xsd");

        kfs.write(kieResources.newClassPathResource("person.xsd", getClass()).setResourceType(ResourceType.XSD).setConfiguration(jaxbConfiguration));

        if (rule != null && rule.length() > 0) {
            kfs.write("src/main/resources/rule.drl", rule);
        }

        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();

        List<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);
        if (!errors.isEmpty()) {
            fail("" + errors);
        }

        KieSession ksession = ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();

        try {
            jndiContext.bind(identifier, ksession);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        return ksession;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        routeBuilder = new RouteBuilder() {
            public void configure() throws Exception {
                JaxbDataFormat def = new JaxbDataFormat();
                def.setPrettyPrint(true);
                // TODO does not work: def.setContextPath( "org.drools.camel.testdomain:org.drools.pipeline.camel" );
                def.setContextPath("org.kie.pipeline.camel");

                from("direct:test-with-session").policy(new KiePolicy()).unmarshal(def).to("kie:ksession1").marshal(def);
            }
        };
        return routeBuilder;
    }
}
