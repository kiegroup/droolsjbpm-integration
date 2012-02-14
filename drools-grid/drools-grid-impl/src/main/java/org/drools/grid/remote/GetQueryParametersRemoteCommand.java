/*
 * Copyright 2012 JBoss by Red Hat.
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
package org.drools.grid.remote;

import org.drools.command.Context;
import org.drools.command.World;
import org.drools.command.impl.GenericCommand;
import org.drools.rule.Declaration;
import org.drools.runtime.rule.impl.NativeQueryResults;

/**
 *
 * @author salaboy
 */
public class GetQueryParametersRemoteCommand implements GenericCommand<String[]>{
     private String queryName;
    private String localId;
    
    public GetQueryParametersRemoteCommand(String queryName, String localId) {
        this.queryName = queryName;
        this.localId = localId;
        
    }
    
    public String[] execute(Context context) {
        Declaration[] parameters = ((org.drools.QueryResults)context.getContextManager().getContext( World.ROOT ).get( this.localId+"-native" )).getParameters();
        String[] results = new String[parameters.length];
        int i = 0;
        for(Declaration param : parameters){
            results[i]=param.getIdentifier();
            i++;
        }
        return results;
    }
}
