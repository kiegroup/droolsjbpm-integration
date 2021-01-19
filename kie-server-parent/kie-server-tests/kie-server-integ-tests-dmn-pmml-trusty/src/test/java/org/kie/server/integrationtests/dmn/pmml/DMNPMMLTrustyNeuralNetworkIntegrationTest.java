package org.kie.server.integrationtests.dmn.pmml;/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServiceResponse;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class DMNPMMLTrustyNeuralNetworkIntegrationTest extends DMNPMMLTrustyKieServerBaseIntegrationTest {

//    private static final ReleaseId kjar1 = new ReleaseId(
//            "org.kie.server.testing", "dmn-pmml-trusty-nn-kjar",
//            "1.0.0.Final" );
//
//    private static final String CONTAINER_1_ID  = "dmn-pml-trusty-neuralnetwork";

    private static final String NEURAL_NETWORK_MODEL_NAMESPACE
            = "https://kiegroup.org/dmn/_51A1FD67-8A67-4332-9889-B718BE8B7456";
    private static final String NEURAL_NETWORK_MODEL_NAME = "NeuralNetworkDMN";
    private static final String NEURAL_NETWORK_DECISION_NAME = "Decision1";
    private static final Object EXPECTED_RESULT;
    private static final Map<String, Object> INPUT_DATA;
    private static final long EXTENDED_TIMEOUT = 300000L;
    // Test setup
    private static final String MODEL_BASE = "nn";
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
        INPUT_DATA.put("Sepal.Length", 5.7);
        INPUT_DATA.put("Sepal.Width", 3.8);
        INPUT_DATA.put("Petal.Length", 1.7);
        INPUT_DATA.put("Petal.Width", 0.3);
        EXPECTED_RESULT = new HashMap<>();
        ((Map)EXPECTED_RESULT).put("Predicted_Species","setosa");
    }


//    @BeforeClass
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

    @Ignore
    @Test
    public void testApplyDmnPmmlNeuralNetworkModelCompiled() {
        execute(CONTAINER_ID_COMPILED,
                NEURAL_NETWORK_MODEL_NAMESPACE,
                NEURAL_NETWORK_MODEL_NAME,
                NEURAL_NETWORK_DECISION_NAME,
                EXPECTED_RESULT,
                INPUT_DATA);
    }

    @Ignore
    @Test
    public void testApplyDmnPmmlNeuralNetworkModelNotCompiled() {
        execute(CONTAINER_ID_NOT_COMPILED,
                NEURAL_NETWORK_MODEL_NAMESPACE,
                NEURAL_NETWORK_MODEL_NAME,
                NEURAL_NETWORK_DECISION_NAME,
                EXPECTED_RESULT,
                INPUT_DATA);
    }
}
