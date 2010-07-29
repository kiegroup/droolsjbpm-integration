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

package org.drools.grid.command;

import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.runtime.impl.ExecutionResultImpl;
import org.drools.grid.generic.NodeData;

public class LookupCommand
    implements
    GenericCommand<String> {

    private String identifier;

    private String outIdentifier;

    public LookupCommand(String identfier) {
        this.identifier = identfier;
    }

    public LookupCommand(String identfier,
                         String outIdentifier) {
        this.identifier = identfier;
        this.outIdentifier = outIdentifier;
    }

    public String execute(Context context) {
        NodeData data = (NodeData) context.get( NodeData.NODE_DATA );

        String instanceId = (String) data.getRoot().get( identifier );

        if ( this.outIdentifier != null ) {
            ((ExecutionResultImpl) ((KnowledgeCommandContext) context).getExecutionResults()).getResults().put( this.outIdentifier,
                                                                                                                instanceId );
        }
        return instanceId;
    }

}
