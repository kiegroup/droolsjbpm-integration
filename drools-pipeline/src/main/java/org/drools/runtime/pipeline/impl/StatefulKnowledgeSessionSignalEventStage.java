/**
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

import org.drools.runtime.pipeline.KnowledgeRuntimeCommand;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Receiver;
import org.drools.runtime.pipeline.StatefulKnowledgeSessionPipelineContext;

public class StatefulKnowledgeSessionSignalEventStage extends BaseEmitter
    implements
    KnowledgeRuntimeCommand {
    private long   id;
    private String eventType;

    public StatefulKnowledgeSessionSignalEventStage(String eventType) {
        this.eventType = eventType;
        this.id = -1;
    }

    public StatefulKnowledgeSessionSignalEventStage(String eventType,
                                                    long id) {
        this.eventType = eventType;
        this.id = id;
    }

    public void receive(Object object,
                        PipelineContext context) {
        StatefulKnowledgeSessionPipelineContext kContext = (StatefulKnowledgeSessionPipelineContext) context;

        if ( this.id != -1 ) {
            kContext.getStatefulKnowledgeSession().getProcessInstance( this.id ).signalEvent( this.eventType,
                                                                                              object );
        } else {
            kContext.getStatefulKnowledgeSession().signalEvent( this.eventType,
                                                                object );
        }

        emit( object,
              kContext );
    }

}
