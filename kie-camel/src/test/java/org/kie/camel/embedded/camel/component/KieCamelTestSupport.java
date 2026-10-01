/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
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
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import jakarta.xml.bind.JAXBContext;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.camel.spi.Registry;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.modelcompiler.ExecutableModelProject;
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

/**
 * Base class for kie-camel embedded tests.
 *
 * <p>Migrated from Camel 2.x JUnit4 + JNDI registry to Camel 4 JUnit5 + Camel registry.
 * Subclasses implement {@link #configureDroolsContext(Registry)} to bind their KieSession
 * instances into the Camel registry instead of a JNDI context.</p>
 */
public abstract class KieCamelTestSupport extends CamelTestSupport {

    protected static final Logger logger = LoggerFactory.getLogger(KieCamelTestSupport.class);

    /** Camel registry — populated via {@link #bindToRegistry(Registry)}. */
    protected Registry camelRegistry;

    protected JAXBContext jaxbContext;

    protected RouteBuilder routeBuilder;

    // -------------------------------------------------------------------------
    // Camel 4 JUnit5 lifecycle
    // -------------------------------------------------------------------------

    /**
     * Called by the Camel JUnit5 extension before routes are started.
     * Delegates to {@link #configureDroolsContext(Registry)} so that subclasses
     * can bind their KieSession/CommandExecutor objects.
     */
    @Override
    protected void bindToRegistry(Registry registry) throws Exception {
        this.camelRegistry = registry;
        configureDroolsContext(registry);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setNormalize(true);
    }

    /**
     * Subclasses bind their KieSession / CommandExecutor objects to the Camel registry here.
     * Replaces the old {@code configureDroolsContext(javax.naming.Context)} that was used
     * in Camel 2.x JNDI-based tests.
     */
    protected abstract void configureDroolsContext(Registry registry);

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a KieSession from a DRL rule string and binds it to the Camel registry
     * under the given identifier.  Call from {@link #configureDroolsContext(Registry)}.
     */
    protected KieSession registerKnowledgeRuntime(String identifier, String rule) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        KieResources kieResources = ks.getResources();

        if (rule != null && rule.length() > 0) {
            kfs.write("src/main/resources/rule.drl", rule);
        }

        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll(ExecutableModelProject.class);

        List<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);
        if (!errors.isEmpty()) {
            fail("" + errors);
        }

        KieSession ksession = ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();

        camelRegistry.bind(identifier, ksession);

        return ksession;
    }

    protected void assertXMLEqual(String expected, String result) throws Exception {
        Diff diff = new Diff(expected, result);
        diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }

    protected void configureDroolsContext() {
        // hook for subclasses that do not need mandatory override
    }

    public JAXBContext getJaxbContext() {
        if (this.jaxbContext == null) {
            JaxbDataFormat def = new JaxbDataFormat();
            def.setPrettyPrint("true");
            def.setContextPath("org.drools.model:org.kie.pipeline.camel");

            // Look up ksession1 from the Camel registry (was JNDI in Camel 2.x)
            KieSession ksession1 = context().getRegistry().lookupByNameAndType("ksession1", KieSession.class);
            if (ksession1 == null) {
                throw new IllegalStateException("No KieSession bound under 'ksession1' in the Camel registry");
            }
            KieBase kbase = ksession1.getKieBase();
            ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(((KnowledgeBaseImpl)kbase).getRootClassLoader());
                def = KiePolicy.augmentJaxbDataFormatDefinition(def);

                // Instantiate JaxbDataFormat runtime directly (Camel 4 removed getDataFormat(RouteContext))
                org.apache.camel.converter.jaxb.JaxbDataFormat jaxbDataformat =
                        new org.apache.camel.converter.jaxb.JaxbDataFormat();
                jaxbDataformat.setContextPath(def.getContextPath());
                jaxbDataformat.setCamelContext(context());
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
