/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.karaf.itest;

import javax.inject.Inject;

import org.apache.karaf.features.FeaturesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class RuntimeManagerFeatureKarafIntegrationTest extends AbstractKarafIntegrationTest {

    @Inject
    protected FeaturesService featuresService;

    @Test
    public void testJbpmRuntimeManager() throws Exception {
        // pre-load BundleWiring.class - will be needed later
        FrameworkUtil.getBundle(this.getClass()).adapt(BundleWiring.class);

        // use class loader that imports jbpm otherwise it will use kie api classloader
        ClassLoader classLoader = this.getClass().getClassLoader();
        // attempt to obtain RuntimeManagerFactory when KIE-API and Drools features are installed, but no jBPM
        RuntimeManagerFactory runtimeManagerFactory = RuntimeManagerFactory.Factory.get(classLoader);
        assertNull("KIE-API created non-null RuntimeManagerFactory when jBPM was not installed.", runtimeManagerFactory);

        // install jBPM feature
        featuresService.installFeature("jbpm");

        // attempt to obtain RuntimeManagerFactory once again, now jBPM is installed so it should succeed
        RuntimeManagerFactory.Factory.reset();
        // we can't use our bundle's classloader obtained above, because it's invalid after installing "jbpm" feature
        // due to refresh operation performed by Karaf 4.resolver
        // however we can get "fresh" classloader of our bundle
        classLoader = FrameworkUtil.getBundle(this.getClass()).adapt(BundleWiring.class).getClassLoader();
        // however, we can't even use RuntimeManagerFactory.Factory.class, because kie-api was also refreshed
        //  - org.kie.api/7.7.1.201807101044 (Wired to org.drools.compiler/7.7.1.201807110808 which is being refreshed)
        //  - org.drools.compiler/7.7.1.201807110808 (Wired to org.apache.servicemix.bundles.spring-context/4.3.16.RELEASE_1 which is being refreshed)
        // even if we preinstall spring-orm (and jpa) features, drools-compiler will be refreshed anyway
        // after installing jbpm feature eventually leading to refresh of kie-api too...
//        runtimeManagerFactory = RuntimeManagerFactory.Factory.get(classLoader);
        String className = System.getProperty("org.jbpm.runtime.manager.class",
                "org.jbpm.runtime.manager.impl.RuntimeManagerFactoryImpl");
        Class<?> clazz = classLoader.loadClass(className);
        assertNotNull("KIE-API created null RuntimeManagerFactory after jBPM was installed.", clazz.newInstance());
    }

    @Configuration
    public static Option[] configure() {
        return new Option[]{
                // Install Karaf Container
                getKarafDistributionOption(),

                // Don't bother with local console output as it just ends up cluttering the logs
                configureConsole().ignoreLocalConsole(),
                // Force the log level to INFO so we have more details during the test.  It defaults to WARN.
                logLevel(LogLevelOption.LogLevel.WARN),

                // Option to be used to do remote debugging
                // debugConfiguration("5005", true),

                // Load drools-module feature only (installs KIE-API and Drools, no jBPM)
                loadKieFeatures("drools-module")
        };
    }
}