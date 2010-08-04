/*
 *  Copyright 2010 salaboy.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.drools.grid.distributed.command;

/**
 *
 * @author salaboy
 */


import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;

public class GetWorkingMemoryEntryPointGridCommand
    implements
    GenericCommand<WorkingMemoryEntryPoint> {

    private String name;

    public GetWorkingMemoryEntryPointGridCommand(String name) {
        this.name = name;
    }

    public WorkingMemoryEntryPoint execute(Context context) {
        StatefulKnowledgeSession ksession = ((KnowledgeCommandContext) context).getStatefulKnowledgesession();
        WorkingMemoryEntryPoint ep = ksession.getWorkingMemoryEntryPoint( name );
        
        context.getContextManager().getDefaultContext().set(name, ep); //setWorkingMemoryEntryPoint( ep );
        //((KnowledgeCommandContext) context.getContextManager() ).set(name, ep);
        return ep;
    }

    public String toString() {
        return "session.getWorkingMemoryEntryPoint( " + name + " );";
    }
}
