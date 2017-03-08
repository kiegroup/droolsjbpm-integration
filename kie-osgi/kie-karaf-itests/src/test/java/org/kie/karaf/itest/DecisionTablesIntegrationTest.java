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

import org.drools.decisiontable.ExternalSpreadsheetCompiler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.api.io.KieResources;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class DecisionTablesIntegrationTest extends AbstractKarafIntegrationTest {

    private static final String SIMPLE_XLS = "/decisiontables/data/MultiSheetDST.xls";
    private static final String SIMPLE_TEMPLATE = "/decisiontables/templates/test_template1.drl";

    private final KieServices ks = KieServices.Factory.get();
    private final KieResources kieResources = ks.getResources();

    private ClassLoader origTCCL;

    @Before
    public void setTCCL() {
        origTCCL = Thread.currentThread().getContextClassLoader();
        // Pax-exam sets the TCCL to the bundle-under-test classloader which in turn means that the XStream marshalling
        // will work with different TCCL than it would with standalone bundle. Setting system/application classloader as
        // TCCL is needed to reproduce the fails related to XStream unmarshalling
        Thread.currentThread().setContextClassLoader(Object.class.getClassLoader());
    }

    @After
    public void restoreTCCL() {
        Thread.currentThread().setContextClassLoader(origTCCL);
    }

    @Test
    public void testBasic() throws Exception {
        ClassLoader bundleClassloader = getClass().getClassLoader();
        final ExternalSpreadsheetCompiler converter = new ExternalSpreadsheetCompiler();
        final String drl = converter.compile( bundleClassloader.getResourceAsStream(SIMPLE_XLS),
                                              bundleClassloader.getResourceAsStream(SIMPLE_TEMPLATE),
                                              11,
                                              2 );
        
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

                loadKieFeatures("drools-decisiontable"),
        };
    }

}
