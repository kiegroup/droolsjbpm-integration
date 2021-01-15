/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.pmml;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;

public class ApplyMiningModelIntegrationTest extends PMMLApplyModelBaseTest {

    private static final String CORRELATION_ID = "123";
    private static final String MODEL_NAME = "MixedMining";
    private static final String FILE_NAME = "MiningModelMixed.pmml";
    private static final String TARGET_FIELD = "categoricalResult";
    private static final Object EXPECTED_RESULT = 2.3724999999999987;
    private static final Map<String, Object> INPUT_DATA;
    private static final long EXTENDED_TIMEOUT = 90000000L;
    // Test setup
    private static final String MODEL_BASE = "mining";
    // Compiled
    private static final String CONTAINER_ID_COMPILED = MODEL_BASE + COMPILED_SUFFIX;
    private static final String ARTIFACT_ID_COMPILED = PMML_TRUSTY_PREFIX + CONTAINER_ID_COMPILED;
    private static final ReleaseId RELEASE_ID_COMPILED = new ReleaseId(TEST_GROUP, ARTIFACT_ID_COMPILED, TEST_VERSION);
    private static final String RESOURCE_COMPILED = KJAR_SOURCES_PREFIX + ARTIFACT_ID_COMPILED;
    // Not Compiled
    private static final String CONTAINER_ID_NOT_COMPILED = MODEL_BASE + NOT_COMPILED_SUFFIX;
    private static final String ARTIFACT_ID_NOT_COMPILED = PMML_TRUSTY_PREFIX + CONTAINER_ID_NOT_COMPILED;
    private static final ReleaseId RELEASE_ID_NOT_COMPILED = new ReleaseId(TEST_GROUP, ARTIFACT_ID_NOT_COMPILED, TEST_VERSION);
    private static final String RESOURCE_NOT_COMPILED = KJAR_SOURCES_PREFIX + ARTIFACT_ID_NOT_COMPILED;


    static {
        INPUT_DATA = new HashMap<>();
        INPUT_DATA.put("categoricalX", "red");
        INPUT_DATA.put("categoricalY", "classA");
        INPUT_DATA.put("age", 25.0);
        INPUT_DATA.put("occupation", "ASTRONAUT");
        INPUT_DATA.put("residenceState", "AP");
        INPUT_DATA.put("validLicense", true);
    }


    @BeforeClass
    public static void buildAndDeployArtifacts() {
        setup(RESOURCE_COMPILED,
              EXTENDED_TIMEOUT,
              CONTAINER_ID_COMPILED,
              RELEASE_ID_COMPILED);
        setup(RESOURCE_NOT_COMPILED,
              EXTENDED_TIMEOUT,
              CONTAINER_ID_NOT_COMPILED,
              RELEASE_ID_NOT_COMPILED);
    }

    @Test
    public void testApplyPmmlMiningModelCompiled() {
        execute(CORRELATION_ID,
                CONTAINER_ID_COMPILED,
                MODEL_NAME,
                FILE_NAME,
                TARGET_FIELD,
                EXPECTED_RESULT,
                INPUT_DATA);
    }

    @Test
    public void testApplyPmmlMiningModelNotCompiled() {
        execute(CORRELATION_ID,
                CONTAINER_ID_NOT_COMPILED,
                MODEL_NAME,
                FILE_NAME,
                TARGET_FIELD,
                EXPECTED_RESULT,
                INPUT_DATA);
    }
}
