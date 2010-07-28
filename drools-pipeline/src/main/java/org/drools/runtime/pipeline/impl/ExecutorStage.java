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

import java.util.Collection;
import java.util.List;

import org.drools.command.Command;
import org.drools.command.CommandFactory;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.pipeline.KnowledgeRuntimeCommand;
import org.drools.runtime.pipeline.PipelineContext;

public class ExecutorStage<T> extends BaseEmitter
    implements
    KnowledgeRuntimeCommand {
    private T result = null;
    public void receive(Object object,
                        PipelineContext context) {
        BasePipelineContext kContext = (BasePipelineContext) context;
        this.result = execute(object, kContext);
        emit( result,
              kContext );
    }
    public T execute(Object object, PipelineContext kContext){
        
        if ( object instanceof Collection ) {
            object = CommandFactory.newBatchExecution( (List<Command>) object );
        }
         
        return ( T ) kContext.getCommandExecutor().execute( (Command) object );
    }

}
