/*
 * Copyright 2012 Red Hat
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

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.scanFeatures;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.UrlReference;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

public class KieSpringIntegrationTestSupport {

    protected static final transient Logger LOG = LoggerFactory.getLogger(KieSpringIntegrationTestSupport.class);
    protected static final String DroolsVersion;
    static { 
        Properties testProps = new Properties();
        try {
            testProps.load(KieSpringIntegrationTestSupport.class.getResourceAsStream("/test.properties"));
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize DroolsVersion property: " + e.getMessage(), e);
        }
        DroolsVersion = testProps.getProperty("project.version");
        LOG.info("Drools Project Version : " + DroolsVersion);
    }

    protected OsgiBundleXmlApplicationContext applicationContext;

    @Inject
    protected BundleContext bc;

    protected void refresh() {
        applicationContext.setBundleContext(bc);
        applicationContext.refresh();
    }

    public static MavenArtifactProvisionOption getFeaturesUrl(String groupId, String artifactId, String version) {
        MavenArtifactProvisionOption mapo =  mavenBundle().groupId(groupId).artifactId(artifactId);
        mapo.type("xml");
        mapo.classifier("features");

        if (version == null) {
            mapo.versionAsInProject();
        } else {
            mapo.version(version);
        }

        LOG.info("Features URL : " + mapo.getURL());

        return mapo;
    }

    public static UrlReference getCamelKarafFeaturesUrl(String version) {
        MavenArtifactProvisionOption mapo = getFeaturesUrl("org.apache.camel.karaf", "apache-camel", version);
        return mapo;
    }

    public static Option loadDroolsKieFeatures(String... features) {
        List<String> result = new ArrayList<String>();
        result.add("drools-module");
        for (String feature : features) {
            result.add(feature);
        }

        return scanFeatures(getFeaturesUrl("org.drools","drools-karaf-features",DroolsVersion), result.toArray(new String[4 + features.length]));
    }

}
