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

package org.kie.server.integrationtests.jbpm;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;

import static org.junit.Assert.*;

public class CustomDataProcessServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.1.Final");

    protected static final String POJO1_CLASS_NAME = "org.jbpm.data.Pojo1";
    protected static final String POJO2_CLASS_NAME = "org.jbpm.data.Pojo2";
    protected static final String POJO3_CLASS_NAME = "org.jbpm.data.Pojo3";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/definition-project-101");

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(POJO1_CLASS_NAME, Class.forName(POJO1_CLASS_NAME, true, kieContainer.getClassLoader()));
        extraClasses.put(POJO2_CLASS_NAME, Class.forName(POJO2_CLASS_NAME, true, kieContainer.getClassLoader()));
        extraClasses.put(POJO3_CLASS_NAME, Class.forName(POJO3_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testStartCheckVariablesAndAbortProcess() throws Exception {
        Class<?> pojo1Class = Class.forName(POJO1_CLASS_NAME, true, kieContainer.getClassLoader());

        Object pojo3 = createInstance(POJO3_CLASS_NAME, "three");
        Object pojo2 = createInstance(POJO2_CLASS_NAME, "two", true, pojo3);
        Object pojo1 = createInstance(POJO1_CLASS_NAME, "one", pojo2);

        Map<String, Object> parameters = new HashMap<String, Object>();

        parameters.put("pojoData", pojo1);

        Long processInstanceId = null;
        try {
            processInstanceId = processClient.startProcess(CONTAINER_ID, "definition-project.usertaskpojo", parameters);

            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);

            Object pojoVariable = processClient.getProcessInstanceVariable(CONTAINER_ID, processInstanceId, "pojoData");
            assertNotNull(pojoVariable);
            assertTrue(pojo1Class.isAssignableFrom(pojoVariable.getClass()));

            assertEquals("one", KieServerReflections.valueOf(pojoVariable, "desc"));

            Object pojo2Variable = KieServerReflections.valueOf(pojoVariable, "pojo2");
            assertNotNull(pojo2Variable);

            assertEquals("two", KieServerReflections.valueOf(pojo2Variable, "desc2"));
            assertEquals(true, KieServerReflections.valueOf(pojo2Variable, "primitiveBoolean"));

            Object pojo3Variable = KieServerReflections.valueOf(pojo2Variable, "pojo3");
            assertNotNull(pojo3Variable);

            assertEquals("three", KieServerReflections.valueOf(pojo3Variable, "desc3"));

        } finally {
            if (processInstanceId != null) {
                processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            }
        }



    }

}
