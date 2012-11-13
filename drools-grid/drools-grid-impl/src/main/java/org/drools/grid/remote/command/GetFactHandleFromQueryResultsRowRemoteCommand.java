/*
 * Copyright 2011 JBoss Inc..
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


import org.drools.command.impl.GenericCommand;
import org.drools.common.DefaultFactHandle;
import org.drools.common.EventFactHandle;
import org.kie.command.Context;
import org.kie.runtime.rule.FactHandle;
import org.kie.runtime.rule.QueryResultsRow;

/**
 *
 * @author salaboy
 */
public class GetFactHandleFromQueryResultsRowRemoteCommand implements GenericCommand<FactHandle>{
    private String queryName;
    private String localId;
    private String rowId;
    private String identifier;
    public GetFactHandleFromQueryResultsRowRemoteCommand(String rowId, String queryName, String localId, String identifier) {
        this.queryName = queryName;
        this.localId = localId;
        this.rowId = rowId;
        this.identifier = identifier;
    }
    
    public FactHandle execute(Context context) {
        FactHandle handle = ((QueryResultsRow) context.get( "Row - "+rowId+" - "+this.localId)).getFactHandle(identifier);
        if(handle instanceof DefaultFactHandle){
            FactHandle disconnectedHandle = ((DefaultFactHandle)handle).clone();
            ((DefaultFactHandle)disconnectedHandle).disconnect();
            return disconnectedHandle;
        }
             
        if(handle instanceof EventFactHandle){
            FactHandle disconnectedHandle = ((EventFactHandle)handle).clone();
            ((EventFactHandle)disconnectedHandle).disconnect();
            return disconnectedHandle;
        }
        return null;
    }
    
}
