/*
 * Copyright 2012 JBoss Inc
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

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.command.World;
import org.drools.examples.carinsurance.domain.Car;
import org.drools.examples.carinsurance.domain.CarType;
import org.drools.examples.carinsurance.domain.Driver;
import org.drools.examples.carinsurance.domain.policy.CoverageType;
import org.drools.examples.carinsurance.domain.request.CoverageRequest;
import org.drools.examples.carinsurance.domain.request.PolicyRequest;
import org.drools.fluent.simulation.SimulationFluent;
import org.drools.fluent.simulation.impl.DefaultSimulationFluent;
import org.drools.io.ResourceFactory;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.math.BigDecimal;

public class PolicyRequestFraudDetectionRulesTest {

    @Test
    public void lyingAboutAge() {
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


        // @formatter:off
        simulationFluent.newPath("init")
            .newStep(0)
                .newKnowledgeBuilder()
                    .add(ResourceFactory.newClassPathResource("org/drools/examples/carinsurance/cep/policyRequestFraudDetectionRules.drl"),
                            ResourceType.DRL)
                    .end(World.ROOT, KnowledgeBuilder.class.getName())
                .newKnowledgeBase()
                    .addKnowledgePackages()
                    .end(World.ROOT, KnowledgeBase.class.getName())
                .newStatefulKnowledgeSession()
                    .end()
            .newStep(1000)
                .getStatefulKnowledgeSession()
                    .insert(realJohn).set("realJohn")
                    .insert(realMini).set("realMini")
                    .insert(realJohnMiniPolicyRequest).set("realJohnMiniPolicyRequest")
                    .fireAllRules()
                    .test("realJohnMiniPolicyRequest.requiresManualApproval == false")
                    .end()
                .end()
            .newStep(5000)
                .getStatefulKnowledgeSession()
                    .insert(fakeJohn).set("fakeJohn")
                    .insert(fakeMini).set("fakeMini")
                    .insert(fakeJohnMiniPolicyRequest).set("fakeJohnMiniPolicyRequest")
                    .fireAllRules()
                    .test("fakeJohnMiniPolicyRequest.requiresManualApproval == true")
                    .end()
                .end()
            .end()
        .runSimulation();
        // @formatter:on
    }

    @Test
    public void notLyingAboutAge() {
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


        // @formatter:off
        simulationFluent.newPath("init")
            .newStep(0)
                .newKnowledgeBuilder()
                    .add(ResourceFactory.newClassPathResource("org/drools/examples/carinsurance/cep/policyRequestFraudDetectionRules.drl"),
                            ResourceType.DRL)
                    .end(World.ROOT, KnowledgeBuilder.class.getName())
                .newKnowledgeBase()
                    .addKnowledgePackages()
                    .end(World.ROOT, KnowledgeBase.class.getName())
                .newStatefulKnowledgeSession()
                    .end()
            .newStep(1000L)
                .getStatefulKnowledgeSession()
                    .insert(realJohn).set("realJohn")
                    .insert(realMini).set("realMini")
                    .insert(realJohnMiniPolicyRequest).set("realJohnMiniPolicyRequest")
                    .fireAllRules()
                    .test("realJohnMiniPolicyRequest.requiresManualApproval == false")
                    .end()
                .end()
            .newStep(2L * 60L * 60L * 1000L)
                .getStatefulKnowledgeSession()
                    .insert(otherJohn).set("otherJohn")
                    .insert(otherMini).set("otherMini")
                    .insert(otherJohnMiniPolicyRequest).set("otherJohnMiniPolicyRequest")
                    .fireAllRules()
                    .test("otherJohnMiniPolicyRequest.requiresManualApproval == false")
                    .end()
                .end()
            .end()
        .runSimulation();
        // @formatter:on
    }

}
