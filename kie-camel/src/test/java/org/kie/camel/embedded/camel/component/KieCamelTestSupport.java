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

import java.util.List;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.io.KieResources;
import org.kie.api.runtime.KieSession;
import org.kie.camel.embedded.component.KiePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class KieCamelTestSupport extends CamelTestSupport {

    protected static final Logger logger = LoggerFactory.getLogger(KieCamelTestSupport.class);

    protected Context jndiContext;

    protected JAXBContext jaxbContext;

    protected RouteBuilder routeBuilder;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setNormalize(true);
    }

    @Override
    protected Context createJndiContext() throws Exception {
        // Overriding this method is necessary in the absence of a spring application context
        // to bootstrap the whole thing. Create another Spring based unit test with all the beans
        // defined as below and remove this comment from here.
        // create
        jndiContext = super.createJndiContext();
        configureDroolsContext(jndiContext);
        return jndiContext;
    }

    protected abstract void configureDroolsContext(Context jndiContext);

    protected KieSession registerKnowledgeRuntime(String identifier, String rule) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        KieResources kieResources = ks.getResources();

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

    protected void assertXMLEqual(String expected, String result) throws Exception {
        Diff diff = new Diff(expected, result);
        diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }

    protected void configureDroolsContext() {
        // TODO Auto-generated method stub

    }

    public JAXBContext getJaxbContext() {
        if (this.jaxbContext == null) {
            JaxbDataFormat def = new JaxbDataFormat();
            def.setPrettyPrint(true);
            // TODO does not work: def.setContextPath( "org.drools.camel.testdomain:org.drools.pipeline.camel" );
            def.setContextPath("org.drools.model:org.kie.pipeline.camel");
            // def.setContextPath( "org.kie.pipeline.camel" );

            // create a jaxbContext for the test to use outside of Camel.
            KieSession ksession1 = null;
            try {
                ksession1 = (KieSession)jndiContext.lookup("ksession1");
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
            KieBase kbase = ksession1.getKieBase();
            ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(((KnowledgeBaseImpl)kbase).getRootClassLoader());
                routeBuilder.getContext().setApplicationContextClassLoader(((KnowledgeBaseImpl)kbase).getRootClassLoader());

                def = KiePolicy.augmentJaxbDataFormatDefinition(def);

                org.apache.camel.converter.jaxb.JaxbDataFormat jaxbDataformat = (org.apache.camel.converter.jaxb.JaxbDataFormat)def.getDataFormat(this.context.getRoutes().get(0)
                    .getRouteContext());

                jaxbDataformat.setCamelContext(routeBuilder.getContext());
                try {
                    jaxbDataformat.start();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                jaxbContext = jaxbDataformat.getContext();
            } finally {
                Thread.currentThread().setContextClassLoader(originalCl);
            }
        }

        return jaxbContext;
    }
}
