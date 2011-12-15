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
import org.drools.command.impl.GenericCommand;
import org.drools.runtime.rule.QueryResultsRow;

/**
 *
 * @author salaboy
 */
public class HasNextQueryResultsRowRemoteCommand implements GenericCommand<Boolean>{
    private String queryName;
    private String localId;
    private String queryResultsId;
    public HasNextQueryResultsRowRemoteCommand(String queryName, String localId) {
        this.queryName = queryName;
        this.localId = localId;
        this.queryResultsId = this.localId + this.queryName;
    }
    
    public Boolean execute(Context context) {
        Iterator<QueryResultsRow>  it = (Iterator<QueryResultsRow>) context.get( "Iterator - "+this.queryResultsId);
        return it.hasNext();
    }
    
}
