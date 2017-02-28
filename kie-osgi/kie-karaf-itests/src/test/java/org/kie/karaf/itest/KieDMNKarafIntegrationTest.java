/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.Results;
import org.kie.api.io.KieResources;
import org.kie.api.runtime.KieContainer;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.internal.utils.KieHelper;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class KieDMNKarafIntegrationTest extends AbstractKarafIntegrationTest {

    private static final String SIMPLE_DMN_FILE = "/kie-dmn/FunctionDefinition.dmn";

    private final KieServices ks = KieServices.Factory.get();
    private final KieResources kieResources = ks.getResources();

    private ClassLoader origTCCL;

    @Before
    public void setTCCL() {
        origTCCL = Thread.currentThread().getContextClassLoader();
        // Pax-exam sets the TCCL to the bundle-under-test classloader which in turn means that the XStream marshalling
        // will work with different TCCL than it would with standalone bundle. Setting system/application classloader as
        // TCCL is needed to reproduce the fails related to XStream unmarshalling in guided-dtables module
        Thread.currentThread().setContextClassLoader(Object.class.getClassLoader());
    }

    @After
    public void restoreTCCL() {
        Thread.currentThread().setContextClassLoader(origTCCL);
    }

    @Test
    public void testDMNbasic() throws Exception {
        DMNRuntime runtime = createKieDMNRuntime(SIMPLE_DMN_FILE);
        assertTrue( runtime.getModels().size() > 0 );
    }

    @Configuration
    public static Option[] configure() {
        return new Option[]{
                // Install Karaf Container
                getKarafDistributionOption(),


                // It is really nice if the container sticks around after the test so you can check the contents
                // of the data directory when things go wrong.
                keepRuntimeFolder(),
                // Don't bother with local console output as it just ends up cluttering the logs
                configureConsole().ignoreLocalConsole(),
                // Force the log level to INFO so we have more details during the test.  It defaults to WARN.
                logLevel(LogLevelOption.LogLevel.WARN),

                // Option to be used to do remote debugging
                //  debugConfiguration("5005", true),

                loadKieFeatures("kie-dmn"),
        };
    }

    private DMNRuntime createKieDMNRuntime(String... resourcePaths) {
        KieHelper kieHelper = new KieHelper();
        ClassLoader bundleClassloader = getClass().getClassLoader();
        kieHelper.setClassLoader(bundleClassloader);
        for (String resourcePath : resourcePaths) {
            kieHelper.addResource(kieResources.newUrlResource(getClass().getResource(resourcePath)));
        }
        Results results = kieHelper.verify();
        Assert.assertTrue(results.toString(), results.getMessages().isEmpty());
        final KieContainer kieContainer = kieHelper.getKieContainer();
        DMNRuntime runtime = kieContainer.newKieSession().getKieRuntime(DMNRuntime.class);
        return runtime;
    }


    private void assertContainsPackage(KieBase kieBase, String packageName) {
        if (kieBase.getKiePackage(packageName) == null) {
            Assert.fail("KieBase with packages [" + kieBase.getKiePackages() + "] does not contain expected package [" +
                    packageName + "]!");
        }
    }

}