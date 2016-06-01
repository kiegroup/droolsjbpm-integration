/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
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

package org.drools.simulation.fluent.session;

import org.drools.simulation.fluent.SimulationFluentBuilder;
import org.drools.simulation.fluent.simulation.SimulationFluent;
import org.drools.simulation.fluent.test.RuleTestFluent;
import org.drools.simulation.fluent.test.TestableFluent;
import org.kie.internal.fluent.ContextFluent;
import org.kie.internal.fluent.EndContextFluent;
import org.kie.internal.fluent.runtime.KieSessionFluent;


public interface KieSessionSimulationFluent
        extends KieSessionFluent<KieSessionSimulationFluent>,
        TestableFluent<KieSessionSimulationFluent>, 
        SimulationFluentBuilder<KieSessionSimulationFluent>,
        ContextFluent<KieSessionSimulationFluent>,
        EndContextFluent<SimulationFluent>,
        RuleTestFluent<KieSessionSimulationFluent> {

}
