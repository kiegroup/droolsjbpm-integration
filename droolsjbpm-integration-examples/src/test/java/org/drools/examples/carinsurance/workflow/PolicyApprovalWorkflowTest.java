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

package org.drools.examples.carinsurance.workflow;

import org.drools.examples.carinsurance.domain.Car;
import org.drools.examples.carinsurance.domain.CarType;
import org.drools.examples.carinsurance.domain.Driver;
import org.drools.examples.carinsurance.domain.policy.CoverageType;
import org.drools.examples.carinsurance.domain.request.CoverageRequest;
import org.drools.examples.carinsurance.domain.request.PolicyRequest;
import org.drools.simulation.fluent.simulation.SimulationFluent;
import org.drools.simulation.fluent.simulation.impl.DefaultSimulationFluent;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.ResourceType;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PolicyApprovalWorkflowTest extends SimulateTestBase {

    @Test
    public void approvePolicyRequest() throws IOException {
        SimulationFluent simulationFluent = new DefaultSimulationFluent();

        Map<String, Object> processParams = new HashMap<String, Object>();

        Driver john = new Driver("John", "Smith", new LocalDate(1970, 1, 1));
        Car mini = new Car("MINI-01", CarType.SMALL, false, new BigDecimal("10000.00"));
        PolicyRequest johnMiniPolicyRequest = new PolicyRequest(john, mini);
        johnMiniPolicyRequest.addCoverageRequest(new CoverageRequest(CoverageType.COLLISION));
        johnMiniPolicyRequest.addCoverageRequest(new CoverageRequest(CoverageType.COMPREHENSIVE));
        processParams.put("policyRequest", johnMiniPolicyRequest);

        assertEquals(false, johnMiniPolicyRequest.isManuallyApproved());
        
        String process = readInputStreamReaderAsString( new InputStreamReader( getClass().getResourceAsStream( "policyRequestWorkflow.bpmn" ) ) );
        ReleaseId releaseId = createKJarWithMultipleResources( "KBase1", new String[]{process}, new ResourceType[] {ResourceType.BPMN2} );
        
        // @formatter:off          
        simulationFluent
        .newKieSession(releaseId, "KBase1.KSession1")
            .startProcess("policyRequestProcess", processParams)
            .end()
        .runSimulation();
        // @formatter:on
        assertEquals(true, johnMiniPolicyRequest.isManuallyApproved());
    }

}
