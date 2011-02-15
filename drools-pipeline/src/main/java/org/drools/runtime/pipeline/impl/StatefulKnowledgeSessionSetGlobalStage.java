/*
 * Copyright 2010 JBoss Inc
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

package org.drools.runtime.pipeline.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.pipeline.KnowledgeRuntimeCommand;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Receiver;
import org.drools.runtime.pipeline.StatefulKnowledgeSessionPipelineContext;

public class StatefulKnowledgeSessionSetGlobalStage extends BaseEmitter
    implements
    KnowledgeRuntimeCommand {
    private String key;
    
    public StatefulKnowledgeSessionSetGlobalStage() {
        
    }
    
    public StatefulKnowledgeSessionSetGlobalStage(String key) {
        this.key = key;
    }

    public void receive(Object object,
                        PipelineContext context) {
        StatefulKnowledgeSessionPipelineContext kContext = (StatefulKnowledgeSessionPipelineContext) context;
        StatefulKnowledgeSession ksession = kContext.getStatefulKnowledgeSession();
        if ( key == null ) {
            if ( !(object instanceof Map) ) {
                throw new IllegalArgumentException( "SetGlobalStage must either declare a key or be an instanceof a Map");
            } else {
                Map<String, Object> vars = ( Map<String, Object> ) object;        
                for ( Entry<String, Object> entry : vars.entrySet()) {
                    ksession.setGlobal( entry.getKey(), entry.getValue() );
                }
            }
        } else {
            ksession.setGlobal( this.key, object );
        }

        
        emit( object,
              kContext );        
    }

}
