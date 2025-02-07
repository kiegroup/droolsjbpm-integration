/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;

public class ApplyRegressionModelIntegrationTest extends PMMLApplyModelBaseTest {

    private static final String CORRELATION_ID = "123";
    private static final String MODEL_NAME = "LinReg";
    private static final String FILE_NAME = "test_regression.pmml";
    private static final String TARGET_FIELD = "fld4";
    private static final BigDecimal EXPECTED_RESULT = BigDecimal.valueOf(1.0);
    private static final Map<String, Object> INPUT_DATA;
    private static final long EXTENDED_TIMEOUT = 30000000L;
    // Test setup
    private static final String MODEL_BASE = "regression";
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
        INPUT_DATA.put("fld1", 12.0);
        INPUT_DATA.put("fld2", 25.0);
        INPUT_DATA.put("fld3", "x");
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
    public void testApplyPmmlRegressionModelCompiled() {
        execute(CORRELATION_ID,
                CONTAINER_ID_COMPILED,
                MODEL_NAME,
                FILE_NAME,
                TARGET_FIELD,
                EXPECTED_RESULT,
                INPUT_DATA);
    }

    @Test
    public void testApplyPmmlRegressionModelNotCompiled() {
        execute(CORRELATION_ID,
                CONTAINER_ID_NOT_COMPILED,
                MODEL_NAME,
                FILE_NAME,
                TARGET_FIELD,
                EXPECTED_RESULT,
                INPUT_DATA);
    }
}
