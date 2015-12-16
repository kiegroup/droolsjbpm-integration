/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.examples.carinsurance.cep;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import org.drools.examples.carinsurance.domain.Car;
import org.drools.examples.carinsurance.domain.CarType;
import org.drools.examples.carinsurance.domain.Driver;
import org.drools.examples.carinsurance.domain.policy.CoverageType;
import org.drools.examples.carinsurance.domain.request.CoverageRequest;
import org.drools.examples.carinsurance.domain.request.PolicyRequest;
import org.drools.examples.carinsurance.workflow.SimulateTestBase;
import org.drools.simulation.fluent.simulation.SimulationFluent;
import org.drools.simulation.fluent.simulation.impl.DefaultSimulationFluent;
import org.joda.time.LocalDate;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.ResourceType;

public class PolicyRequestFraudDetectionRulesTest extends SimulateTestBase {

    @Test @Ignore( "need to fix kjar generation when using existing domain models.")
    public void lyingAboutAge() throws IOException {
        SimulationFluent simulationFluent = new DefaultSimulationFluent();


        Driver realJohn = new Driver("John", "Smith", new LocalDate().minusYears(10));
        Car realMini = new Car("MINI-01", CarType.SMALL, false, new BigDecimal("10000.00"));
        PolicyRequest realJohnMiniPolicyRequest = new PolicyRequest(realJohn, realMini);
        realJohnMiniPolicyRequest.addCoverageRequest(new CoverageRequest(CoverageType.COLLISION));
        realJohnMiniPolicyRequest.addCoverageRequest(new CoverageRequest(CoverageType.COMPREHENSIVE));
        realJohnMiniPolicyRequest.setAutomaticallyRejected(true);
        realJohnMiniPolicyRequest.addRejectedMessage("Too young.");

        Driver fakeJohn = new Driver("John", "Smith", new LocalDate().minusYears(30));
        Car fakeMini = new Car("MINI-01", CarType.SMALL, false, new BigDecimal("10000.00"));
        PolicyRequest fakeJohnMiniPolicyRequest = new PolicyRequest(fakeJohn, fakeMini);
        fakeJohnMiniPolicyRequest.addCoverageRequest(new CoverageRequest(CoverageType.COLLISION));
        fakeJohnMiniPolicyRequest.addCoverageRequest(new CoverageRequest(CoverageType.COMPREHENSIVE));
        fakeJohnMiniPolicyRequest.setAutomaticallyRejected(false);

        String rules = readInputStreamReaderAsString( new InputStreamReader( getClass().getResourceAsStream( "policyRequestFraudDetectionRules.drl" ) ) );
        ReleaseId releaseId = createKJarWithMultipleResources( "org.drools.KBase1", new String[]{rules}, new ResourceType[] {ResourceType.DRL} );

        // @formatter:off
        simulationFluent
        .newStep(1000)
        .newKieSession(releaseId, "org.drools.KBase1.KSession1")
            .insert(realJohn).set("realJohn")
            .insert(realMini).set("realMini")
            .insert(realJohnMiniPolicyRequest).set("realJohnMiniPolicyRequest")
            .fireAllRules()
            .assertRuleFired("lyingAboutAge", 0)
            .test("realJohnMiniPolicyRequest.requiresManualApproval == false")
            .end()
        .newStep(5000)
        .getKieSession()
            .insert(fakeJohn).set("fakeJohn")
            .insert(fakeMini).set("fakeMini")
            .insert(fakeJohnMiniPolicyRequest).set("fakeJohnMiniPolicyRequest")
            .fireAllRules()
            .assertRuleFired("lyingAboutAge", 1)
            .test("fakeJohnMiniPolicyRequest.requiresManualApproval == true")
            .end()
        .runSimulation();
        // @formatter:on
    }

    @Test @Ignore( "need to fix kjar generation when using existing domain models.")
    public void notLyingAboutAge() throws IOException {
        SimulationFluent simulationFluent = new DefaultSimulationFluent();


        Driver realJohn = new Driver("John", "Smith", new LocalDate().minusYears(10));
        Car realMini = new Car("MINI-01", CarType.SMALL, false, new BigDecimal("10000.00"));
        PolicyRequest realJohnMiniPolicyRequest = new PolicyRequest(realJohn, realMini);
        realJohnMiniPolicyRequest.addCoverageRequest(new CoverageRequest(CoverageType.COLLISION));
        realJohnMiniPolicyRequest.addCoverageRequest(new CoverageRequest(CoverageType.COMPREHENSIVE));
        realJohnMiniPolicyRequest.setAutomaticallyRejected(true);
        realJohnMiniPolicyRequest.addRejectedMessage("Too young.");

        Driver otherJohn = new Driver("John", "Smith", new LocalDate().minusYears(30));
        Car otherMini = new Car("MINI-01", CarType.SMALL, false, new BigDecimal("10000.00"));
        PolicyRequest otherJohnMiniPolicyRequest = new PolicyRequest(otherJohn, otherMini);
        otherJohnMiniPolicyRequest.addCoverageRequest(new CoverageRequest(CoverageType.COLLISION));
        otherJohnMiniPolicyRequest.addCoverageRequest(new CoverageRequest(CoverageType.COMPREHENSIVE));
        otherJohnMiniPolicyRequest.setAutomaticallyRejected(false);

        String rules = readInputStreamReaderAsString( new InputStreamReader( getClass().getResourceAsStream( "policyRequestFraudDetectionRules.drl" ) ) );
        ReleaseId releaseId = createKJarWithMultipleResources( "org.drools.KBase1", new String[]{rules}, new ResourceType[] {ResourceType.DRL} );

        // @formatter:off
        simulationFluent.newPath("init")
        .newStep(1000L)
        .newKieSession(releaseId, "org.drools.KBase1.KSession1")
            .insert(realJohn).set("realJohn")
            .insert(realMini).set("realMini")
            .insert(realJohnMiniPolicyRequest).set("realJohnMiniPolicyRequest")
            .fireAllRules()
            .assertRuleFired("lyingAboutAge", 0)
            .test("realJohnMiniPolicyRequest.requiresManualApproval == false")
            .end()
        .newStep(2L * 60L * 60L * 1000L)
        .getKieSession()
            .insert(otherJohn).set("otherJohn")
            .insert(otherMini).set("otherMini")
            .insert(otherJohnMiniPolicyRequest).set("otherJohnMiniPolicyRequest")
            .fireAllRules()
            .assertRuleFired("lyingAboutAge", 0)
            .test("otherJohnMiniPolicyRequest.requiresManualApproval == false")
            .end()
        .runSimulation();
        // @formatter:on
    }

}
