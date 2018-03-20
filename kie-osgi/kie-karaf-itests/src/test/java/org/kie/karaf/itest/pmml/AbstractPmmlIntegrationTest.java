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

package org.kie.karaf.itest.pmml;

import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.core.io.impl.ClassPathResource;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderConfiguration;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.karaf.itest.AbstractKarafIntegrationTest;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;

abstract public class AbstractPmmlIntegrationTest extends AbstractKarafIntegrationTest {

    private static final String PMML = "/pmml/mock_cold_simple.xml";

    public static final <T> T withClassLoader(final ClassLoader classLoader, final Callable<T> callable) throws Exception {

        final ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            return callable.call();
        } finally {
            Thread.currentThread().setContextClassLoader(ccl);
        }
    }

    protected abstract List<String> getKiePmmlFeatures();

    @Configuration
    public Option[] configure() {
        return new Option[]{
                            // Install Karaf Container
                            getKarafDistributionOption(),

                            // Don't bother with local console output as it just ends up cluttering the logs
                            configureConsole().ignoreLocalConsole(),
                            // Force the log level to INFO so we have more details during the test.  It defaults to WARN.
                            logLevel(LogLevelOption.LogLevel.WARN),

                            // Option to be used to do remote debugging
                            //  debugConfiguration("5005", true),

                            loadKieFeatures(getKiePmmlFeatures()),
        };
    }

    private KieSession newSession(Consumer<KnowledgeBuilder> customizer) throws Exception {
        final ClassLoader classLoader = AbstractPmmlIntegrationTest.class.getClassLoader();

        final Properties properties = new Properties();

        final KnowledgeBuilderConfiguration configuration = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(properties, classLoader);

        /*
         * Although the call to newKieBaseConfiguration(Properties, ClassLoader) is deprecated, it is required.
         * Otherwise there is now way to provide the ClassLoader and the compilation will fail with:
         *     java.lang.ClassNotFoundException: org.drools.pmml.pmml_4_2.ModelMarker
         */
        final KieBaseConfiguration baseConfiguration = KieServices.get().newKieBaseConfiguration(properties, classLoader);

        final KnowledgeBuilderImpl builder = (KnowledgeBuilderImpl) KnowledgeBuilderFactory.newKnowledgeBuilder(configuration);
        if (customizer != null) {
            customizer.accept(builder);
        }

        final KieBase base = withClassLoader(classLoader, () -> builder.newKnowledgeBase(baseConfiguration));

        return base.newKieSession();
    }

    /**
     * Test creating a new, empty session.
     */
    @Test
    public void testSetup1() throws Exception {
        newSession(null).dispose();
    }

    /**
     * Test loading a PMML model and fire rules once. 
     */
    protected void testExecute1() throws Exception {
        final KieSession session = newSession(builder -> {
            builder.add(new ClassPathResource(PMML, AbstractPmmlIntegrationTest.class.getClassLoader()), ResourceType.PMML);
        });

        try {
            session.getEntryPoint("in_Temp").insert(22.0);
            session.fireAllRules();
            // we are not evaluating the result here
        } finally {
            session.dispose();
        }
    }

}
