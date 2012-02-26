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

package org.drools.grid.remote.command;

import org.drools.agent.KnowledgeAgent;
import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;
import org.drools.runtime.Environment;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;

public class NewStatefulKnowledgeSessionFromKAgentRemoteCommand
    implements
    GenericCommand<StatefulKnowledgeSession> {

    private KnowledgeSessionConfiguration ksessionConf;
    private Environment environment;
    private String kbaseKagentId;
    public NewStatefulKnowledgeSessionFromKAgentRemoteCommand(KnowledgeSessionConfiguration ksessionConf) {
        this.ksessionConf = ksessionConf;
        
    }
    
    public NewStatefulKnowledgeSessionFromKAgentRemoteCommand(KnowledgeSessionConfiguration ksessionConf,
                                                Environment env, String kbaseKagentId) { 
        this(ksessionConf);
        this.environment = env;
        this.kbaseKagentId = kbaseKagentId;
        
    }
    

    public StatefulKnowledgeSession execute(Context context) {
        
        KnowledgeAgent agent = (KnowledgeAgent) context.getContextManager().getContext("__TEMP__").get(kbaseKagentId+"_kAgent");
        if(agent != null){
            return agent.getKnowledgeBase().newStatefulKnowledgeSession(ksessionConf, environment);
        }
        return null;
    }

}
