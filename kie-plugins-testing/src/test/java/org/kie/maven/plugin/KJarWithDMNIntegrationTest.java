/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.maven.plugin;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.api.DMNFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class KJarWithDMNIntegrationTest extends KieMavenPluginBaseIntegrationTest {

    private final static String GROUP_ID = "org.kie";
    private final static String ARTIFACT_ID = "kie-maven-plugin-test-kjar-7";
    private final static String VERSION = "1.0.0.Final";
    private final static String KJAR_NAME = "kjar-7-with-dmn";

    public KJarWithDMNIntegrationTest(MavenRuntime.MavenRuntimeBuilder builder) {
        super(builder);
    }

    @Test
    public void testCleanInstall() throws Exception {
        buildKJarProject(KJAR_NAME, "clean", "install");
    }

    @Test
    public void testComplexDMNModel() throws Exception {
        testComplexDMNModel(false);
    }

    @Test
    public void testComplexDMNModelWithExecutableModel() throws Exception {
        testComplexDMNModel(true);
    }

    private void testComplexDMNModel(final boolean useExecutableModel) throws Exception {
        if (useExecutableModel) {
            buildKJarProject(KJAR_NAME, "clean", "install", "-DgenerateModel=YES", "-DgenerateDMNModel=YES");
        } else {
            buildKJarProject(KJAR_NAME, "clean", "install");
        }
        final KieServices kieServices = KieServices.get();
        final ReleaseId releaseId = kieServices.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        final KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        final KieSession kieSession = kieContainer.newKieSession();
        try {
            final DMNRuntime dmnRuntime = kieSession.getKieRuntime(DMNRuntime.class);
            final DMNModel dmnModel = dmnRuntime.getModel("http://www.trisotech.com/definitions/_3068644b-d2c7-4b81-ab9d-64f011f81f47",
                                                          "DMN Specification Chapter 11 Example");
            assertThat(dmnModel).isNotNull();
            assertThat(dmnModel.getMessages()).isEmpty();

            final DMNContext context = DMNFactory.newContext();
            context.set("Applicant data", getApplicantData());
            context.set("Bureau data", getBureauData());
            context.set("Requested product", getRequestedProduct());
            context.set("Supporting documents", null);
            final DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, context);
            assertThat(dmnModel.getMessages()).isEmpty();

            final DMNContext result = dmnResult.getContext();
            assertThat(result.get("Strategy")).isEqualTo("THROUGH");
            assertThat(result.get("Routing")).isEqualTo("ACCEPT");
        } finally {
            kieSession.dispose();
        }
    }

    private Map<String, Object> getApplicantData() {
        final Map<String, Object> applicantData = new HashMap<>();
        applicantData.put("Age", new BigDecimal(51));
        applicantData.put("MaritalStatus", "M");
        applicantData.put("EmploymentStatus", "EMPLOYED");
        applicantData.put("ExistingCustomer", false);

        final Map<String, BigDecimal> monthlyMap = new HashMap<>();
        monthlyMap.put("Income", new BigDecimal(100_000));
        monthlyMap.put("Repayments", new BigDecimal(2_500));
        monthlyMap.put("Expenses", new BigDecimal(10_000));

        applicantData.put("Monthly", monthlyMap);
        return applicantData;
    }

    private Map<String, Object> getBureauData() {
        final Map<String, Object> bureauData = new HashMap<>();
        bureauData.put("Bankrupt", false);
        bureauData.put("CreditScore", new BigDecimal(600));
        return bureauData;
    }

    private Map<String, Object> getRequestedProduct() {
        final Map<String, Object> requestedProduct = new HashMap<>();
        requestedProduct.put("ProductType", "STANDARD LOAN");
        requestedProduct.put("Rate", new BigDecimal(0.08));
        requestedProduct.put("Term", new BigDecimal(36));
        requestedProduct.put("Amount", new BigDecimal(100_000));
        return requestedProduct;
    }
}

