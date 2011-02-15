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

import org.drools.common.InternalRuleBase;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.Receiver;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;

public class StatelessKnowledgeSessionPipelineImpl extends BaseEmitter
    implements
    Pipeline {
    private StatelessKnowledgeSession ksession;

    public StatelessKnowledgeSessionPipelineImpl(StatelessKnowledgeSession ksession) {
        this.ksession = ksession;
    }

    public synchronized void insert(Object object,
                                    ResultHandler resultHandler) {
        ClassLoader cl = ((InternalRuleBase) ((StatelessKnowledgeSessionImpl) this.ksession).getRuleBase()).getRootClassLoader();

        StatelessKnowledgeSessionPipelineContextImpl context = new StatelessKnowledgeSessionPipelineContextImpl( ksession,
                                                                                                                 cl,
                                                                                                                 resultHandler );

        emit( object,
              context );
    }

}
