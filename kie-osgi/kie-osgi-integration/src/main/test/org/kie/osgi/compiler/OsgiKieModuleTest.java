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

package org.kie.osgi.compiler;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

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

    @Test
    public void testGatherResourcesFromWARBundle() throws Exception {
        KieServices kieServices = KieServices.Factory.get();
        Bundle bundleMock = Mockito.mock(Bundle.class);
        final List<URL> urls = new ArrayList<URL>();

        // we only care about the path, the file does not have to exist
        // can't mock java.net.URL, because it is a final class
        urls.add(new URL("file:///META-INF/kmodule.xml"));
        urls.add(new URL("file:///WEB-INF/classes/org/org.kie/osgi/SomePOJO.class"));
        urls.add(new URL("file:///WEB-INF/classes/org/org.kie/osgi/some-process.bpmn2"));
        Enumeration<URL> resourcesEnumMock = new Enumeration<URL>() {
            int currentIndex = 0;

            @Override
            public boolean hasMoreElements() {
                return currentIndex < urls.size();
            }

            @Override
            public URL nextElement() {
                return urls.get(currentIndex++);

            }
        };
        Mockito.when(bundleMock.findEntries("", "*", true)).thenReturn(resourcesEnumMock);
        ReleaseId releaseId = kieServices.newReleaseId("org.org.kie.osgi.compiler", "osgi-kie-module-test", "1.0.0.Final");
        OsgiKieModule kmodule = OsgiKieModule.create(releaseId, null, bundleMock);
        Collection<String> fileNames = kmodule.getFileNames();
        Assertions.assertThat(fileNames).hasSize(3);
        Assertions.assertThat(fileNames).contains("META-INF/kmodule.xml", "org/drools/osgi/SomePOJO.class", "org/drools/osgi/some-process.bpmn2");
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
