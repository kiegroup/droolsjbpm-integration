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

import java.util.HashMap;
import java.util.Map;

import org.drools.common.InternalFactHandle;
import org.drools.runtime.pipeline.KnowledgeRuntimeCommand;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Receiver;
import org.drools.runtime.rule.FactHandle;

public class StatefulKnowledgeSessionInsertStage extends BaseEmitter
    implements
    KnowledgeRuntimeCommand {

    public void receive(Object object,
                        PipelineContext context) {
        StatefulKnowledgeSessionPipelineContextImpl kContext = (StatefulKnowledgeSessionPipelineContextImpl) context;
        FactHandle handle = kContext.getEntryPoint().insert( object );
        Map<FactHandle, Object> handles = (Map<FactHandle, Object>)kContext.getResult();
        if ( handles == null ) {
            handles = new HashMap<FactHandle, Object>();
            kContext.setResult( handles );
        }
        
        handles.put( handle,
                     ((InternalFactHandle) handle).getObject() );

        emit( object,
              kContext );
    }

}
