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

import java.util.ArrayList;
import java.util.List;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.UrlReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ops4j.pax.exam.CoreOptions.*;

public class KieSpringIntegrationTestSupport {

    protected static final transient Logger LOG = LoggerFactory.getLogger(KieSpringIntegrationTestSupport.class);
    protected static final String DroolsVersion = "6.0.0-SNAPSHOT";

    public static MavenArtifactProvisionOption getFeatureUrl(String groupId, String camelId) {
        return mavenBundle().groupId(groupId).artifactId(camelId);
    }

    public static UrlReference getCamelKarafFeatureUrl(String version) {
        String type = "xml/features";
        MavenArtifactProvisionOption mavenOption = getFeatureUrl("org.apache.camel.karaf", "apache-camel");
        if (version == null) {
            return mavenOption.versionAsInProject().type(type);
        } else {
            return mavenOption.version(version).type(type);
        }
    }

    public static Option loadDroolsKieFeatures(String... features) {
        List<String> result = new ArrayList<String>();
        result.add("drools-module");
        for (String feature : features) {
            result.add(feature);
        }
        return scanFeatures(getFeatureUrl("org.drools", "drools-karaf-features").type("xml/features").version(DroolsVersion), result.toArray(new String[4 + features.length]));
    }

}
