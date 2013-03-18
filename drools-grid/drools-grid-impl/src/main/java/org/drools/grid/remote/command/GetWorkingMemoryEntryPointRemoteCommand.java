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

package org.drools.grid.remote.command;

/**
 *
 * this class should not exist!
 */

import org.drools.WorkingMemoryEntryPoint;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.impl.KnowledgeCommandContext;
import org.kie.api.command.Context;
import org.kie.api.runtime.StatefulKnowledgeSession;
import org.kie.api.runtime.rule.SessionEntryPoint;

public class GetWorkingMemoryEntryPointRemoteCommand
    implements
    GenericCommand<SessionEntryPoint> {

    private String name;

    public GetWorkingMemoryEntryPointRemoteCommand(String name) {
        this.name = name;
    }

    public WorkingMemoryEntryPoint execute(Context context) {
        StatefulKnowledgeSession ksession = ((KnowledgeCommandContext) context).getStatefulKnowledgesession();
        SessionEntryPoint ep = ksession.getEntryPoint( this.name );
        context.set( this.name,
                                                             ep );
        // If I return the command I need to create a serializable version of NamedEntryPoint
        return null;
    }

    @Override
    public String toString() {
        return "session.getWorkingMemoryEntryPoint( " + this.name + " );";
    }
}
