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
package org.drools.osgi.tests.env;

import static org.knowhowlab.osgi.testing.assertions.BundleAssert.assertBundleAvailable;
import static org.knowhowlab.osgi.testing.assertions.BundleAssert.assertBundleState;
import static org.knowhowlab.osgi.testing.assertions.BundleAssert.assertBundleUnavailable;

import java.util.concurrent.TimeUnit;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class FrameworkBundlesAssertions {

    public static void assertTestBundlesAreInstalled() {

        // assert bundle with symbolic name "org.knowhowlab.osgi.testing.utils"
        // is installed into OSGi framework
        assertBundleAvailable("org.knowhowlab.osgi.testing.utils");
        // assert bundle with symbolic name "org.knowhowlab.osgi.testing.utils"
        // is installed into OSGi framework
        assertBundleState(Bundle.ACTIVE, "org.knowhowlab.osgi.testing.utils",
                5, TimeUnit.SECONDS);
        // assert bundle with symbolic name "org.knowhowlab.osgi.testing.utils"
        // and version "1.2.0"
        // is installed into OSGi framework
        assertBundleAvailable("org.knowhowlab.osgi.testing.utils", new Version(
                "1.2.0"));
        assertBundleAvailable("org.drools.osgi.tests.wrapper", new Version(
                "6.0.0"));
        // assert bundle with symbolic name "org.knowhowlab.osgi.testing.utils"
        // and version "2.0.0"
        // is not installed into OSGi framework
        assertBundleUnavailable("org.knowhowlab.osgi.testing.utils",
                new Version("2.0.0"));
    }

}
