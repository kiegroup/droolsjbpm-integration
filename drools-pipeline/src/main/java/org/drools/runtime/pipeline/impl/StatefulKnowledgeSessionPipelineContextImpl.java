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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.CommandExecutor;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.StatefulKnowledgeSessionPipelineContext;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;

public class StatefulKnowledgeSessionPipelineContextImpl extends BasePipelineContext
    implements
    StatefulKnowledgeSessionPipelineContext {
    private StatefulKnowledgeSession ksession;
    private WorkingMemoryEntryPoint  entryPoint;

    public StatefulKnowledgeSessionPipelineContextImpl(StatefulKnowledgeSession ksession,
                                                       WorkingMemoryEntryPoint entryPoint,
                                                       ResultHandler resultHandler,
                                                       ClassLoader classLoader) {
        super( classLoader,
               resultHandler );
        this.ksession = ksession;
        this.entryPoint = entryPoint;
    }

    public StatefulKnowledgeSession getStatefulKnowledgeSession() {
        return this.ksession;
    }

    public WorkingMemoryEntryPoint getEntryPoint() {
        return entryPoint;
    }

    public CommandExecutor getCommandExecutor() {
        return this.ksession;
    }
    
    
        

}
