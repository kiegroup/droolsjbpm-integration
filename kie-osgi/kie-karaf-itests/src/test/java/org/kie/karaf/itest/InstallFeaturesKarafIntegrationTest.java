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

import org.apache.karaf.features.FeaturesService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExamParameterized;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

/**
 * Serves as a quick smoke test to verify that all the supported KIE features can be successfully installed.
 */
@RunWith(PaxExamParameterized.class)
@ExamReactorStrategy(PerClass.class)
public class InstallFeaturesKarafIntegrationTest extends AbstractKarafIntegrationTest {

    private String featureName;

    @Inject
    protected FeaturesService featuresService;

    public InstallFeaturesKarafIntegrationTest(String featureName) {
        this.featureName = featureName;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static List<Object[]> getParameters() {
        return Arrays.asList(new Object[][]{
                {"drools-common"},
                {"drools-module"},
                //{"drools-templates"}, // TODO: feature install fails, needs to be investigated
                {"drools-decisiontable"},
                {"drools-wb-guided-decisiontables"},
                {"drools-jpa"},
                //{"kie"}, // TODO: feature install fails, needs to be investigated
                //{"kie-ci"}, // TODO: feature install fails, needs to be investigated
                {"kie-spring"},
                {"kie-aries-blueprint"},
                {"jbpm-commons"},
                //{"jbpm-human-task"}, // TODO: feature install fails, needs to be investigated
                {"jbpm"},
                {"jbpm-spring-persistent"},
                //{"droolsjbpm-hibernate"}, // TODO: feature install fails, needs to be investigated
                {"hibernate-validator"},
                //{"h2"}
        });
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

                loadKieFeaturesRepo()
                // Option to be used to do remote debugging
                //  debugConfiguration("5005", true),
        };
    }

    @Test
    public void testInstallFeature() throws Exception {
        featuresService.installFeature(featureName);
        Assert.assertTrue("Feature " + featureName + " not installed!",
                featuresService.isInstalled(featuresService.getFeature(featureName)));

    }

    @After
    public void removeInstalledFeature() throws Exception {
        featuresService.uninstallFeature(featureName);
        Assert.assertFalse("Feature " + featureName + " is still installed, even after explicit call to uninstallFeature()!",
                featuresService.isInstalled(featuresService.getFeature(featureName)));
    }

}
