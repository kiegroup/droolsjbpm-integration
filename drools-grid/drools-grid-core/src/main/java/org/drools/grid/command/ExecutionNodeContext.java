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
import org.drools.command.ContextManager;
import org.drools.grid.generic.NodeData;

public class ExecutionNodeContext
    implements
    Context {
    private Context            context;
    private NodeData data;

    public ExecutionNodeContext(Context context,
                                       NodeData data) {
        this.data = data;
    }

    public NodeData getServiceManagerData() {
        return this.data;
    }

    public ContextManager getContextManager() {
        return context.getContextManager();
    }

    public String getName() {
        return context.getName();
    }

    public Object get(String identifier) {
        return context.get( identifier );
    }

    public void set(String identifier,
                    Object value) {
        context.set( identifier,
                     value );
    }

}
