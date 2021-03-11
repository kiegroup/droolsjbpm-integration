/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.integrationtests.dmn.pmml;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;

public class DMNPMMLTrustyTreeIntegrationTest extends DMNPMMLTrustyKieServerBaseIntegrationTest {

    private static final String TREE_MODEL_NAMESPACE
            = "https://kiegroup.org/dmn/_FAA4232D-9D61-4089-BB05-5F5D7C1AECE1";
    private static final String TREE_MODEL_NAME = "TestTreeDMN";
    private static final String TREE_DECISION_NAME = "Decision";
    private static final Object EXPECTED_RESULT = "sunglasses";
    private static final Map<String, Object> INPUT_DATA;
    private static final long EXTENDED_TIMEOUT = 300000L;

    // Test setup
    private static final String MODEL_BASE = "tree";
    // Compiled
    private static final String CONTAINER_ID_COMPILED = MODEL_BASE + COMPILED_SUFFIX;
    private static final String ARTIFACT_ID_COMPILED = DMN_PMML_TRUSTY_PREFIX + CONTAINER_ID_COMPILED;
    private static final ReleaseId RELEASE_ID_COMPILED = new ReleaseId(TEST_GROUP, ARTIFACT_ID_COMPILED, TEST_VERSION);
    private static final String RESOURCE_COMPILED = KJAR_SOURCES_PREFIX + ARTIFACT_ID_COMPILED;
    // Not Compiled
    private static final String CONTAINER_ID_NOT_COMPILED = MODEL_BASE + NOT_COMPILED_SUFFIX;
    private static final String ARTIFACT_ID_NOT_COMPILED = DMN_PMML_TRUSTY_PREFIX + CONTAINER_ID_NOT_COMPILED;
    private static final ReleaseId RELEASE_ID_NOT_COMPILED = new ReleaseId(TEST_GROUP, ARTIFACT_ID_NOT_COMPILED, TEST_VERSION);
    private static final String RESOURCE_NOT_COMPILED = KJAR_SOURCES_PREFIX + ARTIFACT_ID_NOT_COMPILED;



    static {
        INPUT_DATA = new HashMap<>();
        INPUT_DATA.put("temperature", 30);
        INPUT_DATA.put("humidity", 10);
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
    public void testApplyDmnPmmlTreeModelCompiled() {
        execute(CONTAINER_ID_COMPILED,
                TREE_MODEL_NAMESPACE,
                TREE_MODEL_NAME,
                TREE_DECISION_NAME,
                EXPECTED_RESULT,
                INPUT_DATA);
    }

    @Test
    public void testApplyDmnPmmlTreeModelNotCompiled() {
        execute(CONTAINER_ID_NOT_COMPILED,
                TREE_MODEL_NAMESPACE,
                TREE_MODEL_NAME,
                TREE_DECISION_NAME,
                EXPECTED_RESULT,
                INPUT_DATA);
    }
}
