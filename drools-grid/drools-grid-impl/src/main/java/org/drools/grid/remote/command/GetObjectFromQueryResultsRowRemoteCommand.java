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


import java.util.Iterator;
import org.drools.command.Context;
import org.drools.command.World;
import org.drools.command.impl.GenericCommand;
import org.drools.runtime.rule.QueryResultsRow;

/**
 *
 * @author salaboy
 */
public class GetObjectFromQueryResultsRowRemoteCommand implements GenericCommand<Object>{
    private String queryName;
    private String localId;
    private String rowId;
    private String identifier;
    public GetObjectFromQueryResultsRowRemoteCommand(String rowId, String queryName, String localId, String identifier) {
        this.queryName = queryName;
        this.localId = localId;
        this.rowId = rowId;
        this.identifier = identifier;
    }
    
    public Object execute(Context context) {
        return ((QueryResultsRow) context.getContextManager().getContext( World.ROOT ).get( "Row - "+rowId+" - "+this.localId)).get(identifier);
        
    }
    
}
