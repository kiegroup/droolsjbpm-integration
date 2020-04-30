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

package org.jbpm.simulation;

import java.util.Map;

import org.jbpm.simulation.impl.SimulationPath;
import org.kie.api.definition.process.Node;

public interface SimulationDataProvider {

    Map<String, Object> getSimulationDataForNode(Node node);
    
    double calculatePathProbability(SimulationPath path);

    Map<String, Object> getProcessDataForNode(Node node);
}
