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

package org.drools.jboss.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static junit.framework.TestCase.assertNotNull;

public class FullDistributionTest {

    private static final Logger logger = LoggerFactory.getLogger(FullDistributionTest.class);

    public static final String DIST_FILE = "distFile";

    public static WebArchive createDeploymentForDistribution(String distFile) throws Exception {

        WebArchive webArchive = ShrinkWrap.create(WebArchive.class);
        JarFile jar = new JarFile(distFile);
        JarEntry entry;
        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements() && (entry = entries.nextElement()) != null) {
            if (!entry.isDirectory()) {
                webArchive.add(getAsset(jar, entry), entry.getName());
            }
        }
        webArchive.addClass(FullDistributionTest.class);
        System.out.println("Generated web archive");
        System.out.println("*******************************");
        System.out.println(webArchive.toString(true));
        System.out.println("*******************************");

        return webArchive;
    }

    public static Asset getAsset(final JarFile jar, final JarEntry entry) {
        return new Asset() {
            @Override
            public InputStream openStream() {
                try {
                    return jar.getInputStream(entry);
                } catch (Exception e) {
                    logger.error("asset generation failed. entry: " + entry, e);
                    //don't worry It's just a test
                    return null;
                }
            }
        };
    }

    @Deployment
    public static WebArchive createDeployment() {
        String distFile = System.getProperty(DIST_FILE);
        logger.info("Configured distribution file is: " + distFile);
        assertNotNull("Distribution file was not configured", distFile);

        WebArchive distWar = null;
        try {
            distWar = createDeploymentForDistribution(distFile);
        } catch (Exception e) {
            logger.error("WebArchive creation failed, fistFile: "+ distFile, e);
        }
        assertNotNull("WebArchive couldn't be created.", distWar);
        return distWar;
    }

}
