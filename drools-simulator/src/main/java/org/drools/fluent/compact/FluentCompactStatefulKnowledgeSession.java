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

package org.drools.fluent.compact;

import org.drools.fluent.FluentStatefulKnowledgeSession;
import org.drools.fluent.FluentTest;


public interface FluentCompactStatefulKnowledgeSession  extends FluentStatefulKnowledgeSession<FluentCompactStatefulKnowledgeSession>, FluentTest<FluentCompactStatefulKnowledgeSession> { 
    FluentCompactStatefulKnowledgeSession newStep(long distance);
    
    /**
     * The knowledge base is already created and attached to the KnowledgeBuilder, so all kbuilder changes are automatically reflected in kbase.
     * @return
     */
    FluentCompactKnowledgeBase getKnowledgeBase();     
    
    /**
     * The last executed command, if it returns a value, is set to a name in this executings context
     * @param name
     * @return
     */
    FluentCompactStatefulKnowledgeSession set(String name);
           
    /**
     * The contexts for tests will still refer to the path created by this ksession, until a new ksession is created
     * @return
     */
    FluentCompactSimulation end();
}
