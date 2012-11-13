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

import org.drools.command.impl.GenericCommand;
import org.drools.runtime.rule.impl.NativeQueryResults;
import org.kie.command.Context;

/**
 *
 * @author salaboy
 */
public class GetQueryObjectRemoteCommand implements GenericCommand<Object>{
    private String localId;
    private String key;
    public GetQueryObjectRemoteCommand(String localId, String key) {
        this.localId = localId;
        this.key = key;
    }
    
    
    
    public Object execute(Context context) {
        Object result = ((NativeQueryResults)context.getContextManager().getContext( "__TEMP__" ).get( this.localId )).getResults().get(0).get(key);
        return result;
    }
    
}
