/*
 * Copyright 2011 JBoss Inc
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

package org.drools.simulation.fluent.batch;


import org.drools.simulation.fluent.session.StatefulRuleSessionFluent;
import org.kie.command.BatchExecutionCommand;
import org.kie.internal.fluent.FluentRoot;

// TODO Do we really want this as a separate class hierarchy just to do batches? Does this fit in with the SimulationFluent?
public interface FluentBatchExecution extends FluentRoot, StatefulRuleSessionFluent<FluentBatchExecution> {

    FluentBatchExecution newBatchExecution();
    BatchExecutionCommand getBatchExecution();

}
