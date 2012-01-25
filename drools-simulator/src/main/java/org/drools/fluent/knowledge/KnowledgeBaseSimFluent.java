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

package org.drools.fluent.knowledge;

import org.drools.fluent.FluentKnowledgeBase;
import org.drools.fluent.FluentTest;
import org.drools.fluent.session.StatefulKnowledgeSessionSimFluent;
import org.drools.fluent.simulation.SimulationStepFluent;


public interface KnowledgeBaseSimFluent extends FluentKnowledgeBase<KnowledgeBaseSimFluent>, FluentTest<KnowledgeBaseSimFluent> {
    
    StatefulKnowledgeSessionSimFluent newStatefulKnowledgeSession();
    
    SimulationStepFluent end(String context, String name);
    
    SimulationStepFluent end(String name);
    
    SimulationStepFluent end();
        
}
