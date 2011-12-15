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

package org.drools.fluent;

import org.drools.fluent.compact.FluentCompactStatefulKnowledgeSession;

public interface FluentStatefulKnowledgeSession<T>  extends FluentBase  {
            
    T fireAllRules();
    
    T insert(Object object);  
    
    T setGlobal( String identifier, Object object );
    
    /**
     * The last executed command, if it returns a value, is set to a name in this executings context
     * @param name
     * @return
     */
    FluentStatefulKnowledgeSession<T> set(String name);    
}
