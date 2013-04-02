/*
 * Copyright 2013 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.osgi.tests.equinox.knoplerfish;

import static org.ops4j.pax.exam.CoreOptions.options;

import org.drools.osgi.tests.common.CommonPaxExamConfiguration;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

public class BundlesIntegrationTest extends AbstractTest {

    @Configuration
    public static Option[] customTestConfiguration() {
        return options(CommonPaxExamConfiguration.baseDroolsConfiguration());
    }

    @Test
    public void assertTestFrameworkBundles() {
        getTestAssertions().assertTestBundlesAreInstalled();
    }
}
