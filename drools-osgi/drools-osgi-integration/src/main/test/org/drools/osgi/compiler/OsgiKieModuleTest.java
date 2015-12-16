/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.drools.osgi.compiler;

import org.junit.Assert;
import org.junit.Test;

public class OsgiKieModuleTest {

    @Test
    public void testAcceptsValidOsgiBundleURLs() {
        // Felix format
        assertAcceptsStringAsOsgiBundleUrL("bundle://something");
        // Equinox format
        assertAcceptsStringAsOsgiBundleUrL("bundleresource://something");
        // Equinox + blueprint format
        assertAcceptsStringAsOsgiBundleUrL("bundleentry://something");
        // other possible formats starting with "bundle"
        assertAcceptsStringAsOsgiBundleUrL("bundle-something://something-else");
    }

    @Test
    public void testRejectsInvalidOsgiBundleUrls() {
        assertRejectsStringAsOsgiBundleUrRL("something://else");
        assertRejectsStringAsOsgiBundleUrRL("mybundle://invalid");
    }

    @Test
    public void testParsingOfBundleIdFromOsgiURL() {
        // Felix bundle URL
        assertBundleIdCorrectlyParsed("bundle://130.0:1/", "130");
        // Equinox bundle URL
        assertBundleIdCorrectlyParsed("bundleresource://151.fwk495985218/", "151");
        // invalid bundle URL results in "null"
        assertBundleIdCorrectlyParsed("invalid-bundle-url", null);
    }

    private void assertAcceptsStringAsOsgiBundleUrL(String str) {
        Assert.assertTrue("String '" + str + "' not recognized as OSGi bundle URL!", OsgiKieModule.isOsgiBundleUrl(str));
    }

    private void assertRejectsStringAsOsgiBundleUrRL(String str) {
        Assert.assertFalse("Invalid string '" + str + "' recognized as OSGi bundle URL!", OsgiKieModule.isOsgiBundleUrl(str));
    }

    private void assertBundleIdCorrectlyParsed(String bundleUrl, String expectedBundleId) {
        Assert.assertEquals(expectedBundleId, OsgiKieModule.parseBundleId(bundleUrl));
    }

}
