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
    protected org.apache.camel.spi.Registry createCamelRegistry() throws Exception {
        org.apache.camel.spi.Registry registry = super.createCamelRegistry();
        this.jndiContext = new RegistryContext(registry);
        configureDroolsContext(this.jndiContext);
        return registry;
    }

    protected abstract void configureDroolsContext(Context jndiContext);

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
            def.setPrettyPrint("true");
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

                org.apache.camel.converter.jaxb.JaxbDataFormat jaxbDataformat = (org.apache.camel.converter.jaxb.JaxbDataFormat)def.getDataFormat();

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


    protected static class RegistryContext implements javax.naming.Context {

        private final org.apache.camel.spi.Registry registry;

        RegistryContext(org.apache.camel.spi.Registry registry) {
            this.registry = registry;
        }

        @Override
        public Object lookup(String name) throws javax.naming.NamingException {
            return registry.lookupByName(name);
        }

        @Override
        public Object lookup(javax.naming.Name name) throws javax.naming.NamingException {
            return lookup(name.toString());
        }

        @Override
        public void bind(String name, Object obj) throws javax.naming.NamingException {
            registry.bind(name, obj);
        }

        @Override
        public void bind(javax.naming.Name name, Object obj) throws javax.naming.NamingException {
            bind(name.toString(), obj);
        }

        @Override
        public void rebind(String name, Object obj) throws javax.naming.NamingException {
            bind(name, obj);
        }

        @Override
        public void rebind(javax.naming.Name name, Object obj) throws javax.naming.NamingException {
            bind(name.toString(), obj);
        }

        @Override
        public void unbind(String name) throws javax.naming.NamingException {
            throw new javax.naming.OperationNotSupportedException();
        }

        @Override
        public void unbind(javax.naming.Name name) throws javax.naming.NamingException {
            unbind(name.toString());
        }

        @Override
        public void rename(String oldName, String newName) throws javax.naming.NamingException {
            throw new javax.naming.OperationNotSupportedException();
        }

        @Override
        public void rename(javax.naming.Name oldName, javax.naming.Name newName) throws javax.naming.NamingException {
            rename(oldName.toString(), newName.toString());
        }

        @Override
        public javax.naming.NamingEnumeration<javax.naming.NameClassPair> list(String name)
                throws javax.naming.NamingException {
            throw new javax.naming.OperationNotSupportedException();
        }

        @Override
        public javax.naming.NamingEnumeration<javax.naming.NameClassPair> list(javax.naming.Name name)
                throws javax.naming.NamingException {
            return list(name.toString());
        }

        @Override
        public javax.naming.NamingEnumeration<javax.naming.Binding> listBindings(String name)
                throws javax.naming.NamingException {
            throw new javax.naming.OperationNotSupportedException();
        }

        @Override
        public javax.naming.NamingEnumeration<javax.naming.Binding> listBindings(javax.naming.Name name)
                throws javax.naming.NamingException {
            return listBindings(name.toString());
        }

        @Override
        public void destroySubcontext(String name) throws javax.naming.NamingException {
            throw new javax.naming.OperationNotSupportedException();
        }

        @Override
        public void destroySubcontext(javax.naming.Name name) throws javax.naming.NamingException {
            destroySubcontext(name.toString());
        }

        @Override
        public javax.naming.Context createSubcontext(String name) throws javax.naming.NamingException {
            throw new javax.naming.OperationNotSupportedException();
        }

        @Override
        public javax.naming.Context createSubcontext(javax.naming.Name name) throws javax.naming.NamingException {
            return createSubcontext(name.toString());
        }

        @Override
        public Object lookupLink(String name) throws javax.naming.NamingException {
            return lookup(name);
        }

        @Override
        public Object lookupLink(javax.naming.Name name) throws javax.naming.NamingException {
            return lookup(name.toString());
        }

        @Override
        public javax.naming.NameParser getNameParser(String name) throws javax.naming.NamingException {
            throw new javax.naming.OperationNotSupportedException();
        }

        @Override
        public javax.naming.NameParser getNameParser(javax.naming.Name name) throws javax.naming.NamingException {
            return getNameParser(name.toString());
        }

        @Override
        public javax.naming.Name composeName(javax.naming.Name name, javax.naming.Name prefix)
                throws javax.naming.NamingException {
            throw new javax.naming.OperationNotSupportedException();
        }

        @Override
        public String composeName(String name, String prefix) throws javax.naming.NamingException {
            throw new javax.naming.OperationNotSupportedException();
        }

        @Override
        public Object addToEnvironment(String propName, Object propVal) throws javax.naming.NamingException {
            return null;
        }

        @Override
        public Object removeFromEnvironment(String propName) throws javax.naming.NamingException {
            return null;
        }

        @Override
        public java.util.Hashtable<?, ?> getEnvironment() throws javax.naming.NamingException {
            return new java.util.Hashtable<>();
        }

        @Override
        public void close() throws javax.naming.NamingException {
            // no-op
        }

        @Override
        public String getNameInNamespace() throws javax.naming.NamingException {
            return "";
        }
    }
}
