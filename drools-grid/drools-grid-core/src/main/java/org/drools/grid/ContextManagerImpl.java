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

/**
 * 
 */
package org.drools.grid;

import java.util.HashMap;
import java.util.Map;

import org.drools.command.Context;
import org.drools.command.ContextManager;

public class ContextManagerImpl
    implements
    ContextManager {
    private Map<String, Context> contexts;
    private Context              defaultContext;

    public ContextManagerImpl() {
        this.contexts = new HashMap<String, Context>();
    }

    public synchronized void addContext(Context context) {
        if ( this.contexts.isEmpty() ) {
            this.defaultContext = context;
        }
        this.contexts.put( context.getName(),
                           context );
    }

    public synchronized Context getContext(String identifier) {
        return this.contexts.get( identifier );
    }

    public Context getDefaultContext() {
        return this.defaultContext;
    }
}