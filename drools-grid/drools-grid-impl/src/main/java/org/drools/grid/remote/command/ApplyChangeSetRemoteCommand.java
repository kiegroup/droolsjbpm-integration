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

import org.drools.ChangeSet;
import org.drools.agent.KnowledgeAgent;
import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;
import org.drools.io.Resource;

public class ApplyChangeSetRemoteCommand
    implements
    GenericCommand<Void> {

    
    
    private String kbaseKagentId;
    private ChangeSet cs;
    private Resource res;
    
    public ApplyChangeSetRemoteCommand( String kbaseKagentId, ChangeSet cs) { 
        this.kbaseKagentId = kbaseKagentId;
        this.cs = cs;
    }
    
    public ApplyChangeSetRemoteCommand( String kbaseKagentId, Resource res) { 
        this.kbaseKagentId = kbaseKagentId;
        this.res = res;
    }
    

    public Void execute(Context context) {
        
        
        KnowledgeAgent agent = (KnowledgeAgent) context.getContextManager().getContext("__TEMP__").get(kbaseKagentId+"_kAgent");
        if(this.res != null){
            System.out.println("Applying Resource: "+this.res);
            agent.applyChangeSet(this.res);
        }
        if(this.cs != null){
            System.out.println("Applying Change-set: "+this.cs);
            agent.applyChangeSet(this.cs);
        }
        return null;
    }

}
