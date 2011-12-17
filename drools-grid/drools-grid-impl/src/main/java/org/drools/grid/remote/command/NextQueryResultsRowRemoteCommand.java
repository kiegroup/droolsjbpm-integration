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
import java.util.UUID;
import org.drools.command.Context;
import org.drools.command.ContextManager;
import org.drools.command.impl.GenericCommand;
import org.drools.runtime.rule.QueryResultsRow;

/**
 *
 * @author salaboy
 */
public class NextQueryResultsRowRemoteCommand implements GenericCommand<String>{
    private String queryName;
    private String localId;
    private String queryResultsId;
    public NextQueryResultsRowRemoteCommand(String queryName, String localId) {
        this.queryName = queryName;
        this.localId = localId;
        this.queryResultsId = this.localId + this.queryName;
    }
    
    public String execute(Context context) {
        String rowId = UUID.randomUUID().toString();
<<<<<<< HEAD
        QueryResultsRow row = ((Iterator<QueryResultsRow>) context.getContextManager().getDefaultContext().get( "Iterator - "+this.queryResultsId)).next();
        context.getContextManager().getDefaultContext().set("Row - "+rowId+" - "+this.queryResultsId, row);
=======
        QueryResultsRow row = ((Iterator<QueryResultsRow>) context.getContextManager().getContext( ContextManager.ROOT ).get( "Iterator - "+this.queryResultsId)).next();
        context.set("Row - "+rowId+" - "+this.queryResultsId, row);
>>>>>>> 8bc485d... JBRULES-3315 Remove drools-grid from spring
        return rowId;
        
    }
    
}
