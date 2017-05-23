/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.integration.testcoverage.instrumentation.separate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Generic utilities used by tests, mainly for getting current project version.
 */
public class TestUtil {

    public static final String TEST_PROPERTIES_FILE = "/test.properties";

    private static final String PROJECT_VERSION_PROPERTY = "project.version";

    private static final transient Logger logger = LoggerFactory.getLogger(TestUtil.class);

    private static final String PROJECT_VERSION = loadProjectVersion();

    public static String getProjectVersion() {
        return PROJECT_VERSION;
    }

    private static String loadProjectVersion() {
        Properties testProps = new Properties();
        try {
            testProps.load(TestUtil.class.getResourceAsStream(TEST_PROPERTIES_FILE));
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize PROJECT_VERSION property: " + e.getMessage(), e);
        }

        final String projectVersion = testProps.getProperty(PROJECT_VERSION_PROPERTY);
        logger.info("Loaded Project Version: " + projectVersion);
        return projectVersion;
    }
}
