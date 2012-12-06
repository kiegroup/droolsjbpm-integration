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

import org.drools.command.impl.GenericCommand;
import org.kie.command.Context;
import org.kie.runtime.rule.QueryResults;
import org.kie.runtime.rule.QueryResultsRow;

/**
 *
 * @author salaboy
 */
public class SetQueryIteratorRemoteCommand implements GenericCommand<Void>{
    private String queryName;
    private String localId;
    public SetQueryIteratorRemoteCommand(String queryName, String localId) {
        this.queryName = queryName;
        this.localId = localId;
        
    }
    
    public Void execute(Context context) {
        Iterator<QueryResultsRow>  it = ((QueryResults)context.getContextManager().getContext( "__TEMP__" ).get( this.localId )).iterator();
        context.getContextManager().getContext( "__TEMP__" ).set( "Iterator - "+this.localId, it);
        return null;
    }
    
}
