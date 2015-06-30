/*
 * Copyright 2015 Red Hat
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.drools.karaf.itest;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Properties;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;

abstract public class AbstractKarafIntegrationTest {

    private static final transient Logger logger = LoggerFactory.getLogger(AbstractKarafIntegrationTest.class);

    protected static final String DROOLS_VERSION;

    static {
        Properties testProps = new Properties();
        try {
            testProps.load(AbstractKieSpringKarafIntegrationTest.class.getResourceAsStream("/test.properties"));
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize DROOLS_VERSION property: " + e.getMessage(), e);
        }
        DROOLS_VERSION = testProps.getProperty("project.version");
        logger.info("Drools Project Version : " + DROOLS_VERSION);
    }

    @Inject
    protected BundleContext bundleContext;

    protected Bundle getInstalledBundle(String symbolicName) {
        for (Bundle b : bundleContext.getBundles()) {
            if (b.getSymbolicName().equals(symbolicName)) {
                return b;
            }
        }
        for (Bundle b : bundleContext.getBundles()) {
            logger.warn("Bundle: " + b.getSymbolicName());
        }
        throw new RuntimeException("Bundle " + symbolicName + " does not exist");
    }

    private static String getKarafVersion() {
        String karafVersion = System.getProperty("karafVersion");
        if (karafVersion == null) {
            // setup the default version of it
            karafVersion = "2.4.2";
        }
        return karafVersion;
    }

    public static Option getKarafDistributionOption() {
        String karafVersion = getKarafVersion();
        logger.info("*** The karaf version is " + karafVersion + " ***");
        return new DefaultCompositeOption(karafDistributionConfiguration()
                .frameworkUrl(maven().groupId("org.apache.karaf").artifactId("apache-karaf").type("tar.gz").versionAsInProject())
                .karafVersion(karafVersion)
                .name("Apache Karaf")
                .useDeployFolder(false).unpackDirectory(new File("target/paxexam/unpack/")),
                localMavenRepoOption(),
                editConfigurationFilePut("etc/org.ops4j.pax.url.mvn.cfg", "org.ops4j.pax.url.mvn.repositories",
                        "http://repo1.maven.org/maven2@id=central," +
                        "https://repository.jboss.org/nexus/content/groups/public@id=jboss-public"
                ));

    }

    public static Option localMavenRepoOption() {
        String localRepo = System.getProperty("maven.repo.local", "");
        if (localRepo.length() > 0) {
            logger.info("Using alternative local Maven repository in {}.", new File(localRepo).getAbsolutePath());
        }
        return when(localRepo.length() > 0).useOptions(
                //                systemProperty("org.ops4j.pax.url.mvn.localRepository").value(new File(localRepo).getAbsolutePath()));
                editConfigurationFilePut("etc/org.ops4j.pax.url.mvn.cfg",
                        "org.ops4j.pax.url.mvn.localRepository",
                        new File(localRepo).getAbsolutePath()));
    }

    public static MavenArtifactProvisionOption getFeaturesUrl(String groupId, String artifactId, String version) {
        MavenArtifactProvisionOption mapo = mavenBundle().groupId(groupId).artifactId(artifactId);
        mapo.type("xml");
        mapo.classifier("features");

        if (version == null) {
            mapo.versionAsInProject();
        } else {
            mapo.version(version);
        }

        logger.info("Features URL: " + mapo.getURL());

        return mapo;
    }

    public static Option loadKieFeaturesRepo() {
        return features(maven().groupId("org.drools").artifactId("drools-karaf-features").type("xml").classifier("features").versionAsInProject().getURL());
    }

    public static Option loadKieFeatures(String... features) {
        MavenArtifactProvisionOption featuresUrl = getFeaturesUrl("org.drools", "drools-karaf-features", DROOLS_VERSION);
        return features(featuresUrl, features);
    }

    public static Option loadKieFeatures(List<String> features) {
        return loadKieFeatures(features.toArray(new String[features.size()]));
    }

}
