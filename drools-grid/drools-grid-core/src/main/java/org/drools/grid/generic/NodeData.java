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

package org.drools.grid.generic;

import java.util.concurrent.atomic.AtomicInteger;

import org.drools.command.Context;
import org.drools.command.ContextManager;
import org.drools.command.impl.ContextImpl;
import org.drools.grid.ContextManagerImpl;

public class NodeData {
    private ContextManager contextManager;

    private Context        root;
    private Context        temp;

    public static String   ROOT                 = "ROOT";
    public static String   TEMP                 = "__TEMP__";
    public static String   NODE_DATA = "__NodeData__";

    private AtomicInteger  sessionIdCounter     = new AtomicInteger();

    public NodeData() {
        // Setup ROOT context, this will hold all long lived intances and instanceIds
        this.contextManager = new ContextManagerImpl();

        this.root = new ContextImpl( ROOT,
                                     this.contextManager );
        ((ContextManagerImpl) this.contextManager).addContext( this.root );
        this.root.set( NODE_DATA,
                       this );
        // Setup TEMP context, this will hold all short lived instanceId and instances
        // TODO: TEMP context should have a time/utilisation eviction queue added 
        this.temp = new ContextImpl( TEMP,
                                     this.contextManager,
                                     this.root );
        ((ContextManagerImpl) this.contextManager).addContext( this.temp );
    }

    public AtomicInteger getSessionIdCounter() {
        return sessionIdCounter;
    }

    public ContextManager getContextManager() {
        return contextManager;
    }

    public void setContextManager(ContextManager contextManager) {
        this.contextManager = contextManager;
    }

    public Context getRoot() {
        return root;
    }

    public void setRoot(Context root) {
        this.root = root;
    }

    public Context getTemp() {
        return temp;
    }

    public void setTemp(Context temp) {
        this.temp = temp;
    }

}
