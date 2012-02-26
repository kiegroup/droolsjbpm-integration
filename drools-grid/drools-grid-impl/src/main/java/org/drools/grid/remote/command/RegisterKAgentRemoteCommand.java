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

import org.drools.KnowledgeBase;
import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.runtime.Environment;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.agent.KnowledgeAgentConfiguration;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.agent.KnowledgeAgent;
import org.drools.command.Context;
public class RegisterKAgentRemoteCommand
    implements
    GenericCommand<KnowledgeAgent> {

    private String kAgentId;
    

    public RegisterKAgentRemoteCommand(String kAgentId) {
        this.kAgentId = kAgentId;
        
    }

    public KnowledgeAgent execute(Context context) {
        KnowledgeBase kbase = ((KnowledgeCommandContext) context).getKnowledgeBase();
        KnowledgeAgentConfiguration kaConfig = KnowledgeAgentFactory.newKnowledgeAgentConfiguration();
        kaConfig.setProperty("drools.agent.newInstance", "false");
        return KnowledgeAgentFactory.newKnowledgeAgent(this.kAgentId, kbase, kaConfig);
    }

}
