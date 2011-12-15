/*
 * Copyright 2011 JBoss Inc
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

package org.drools.simulation.impl;

import org.drools.command.Context;
import org.drools.command.GetDefaultValue;
import org.drools.command.impl.GenericCommand;

public class PrintVariableCommand
    implements
    GenericCommand<Void> {
    private String identifier;
    private String contextName;

    public PrintVariableCommand(String contextName,
                                String identifier) {
        this.identifier = identifier;
        this.contextName = contextName;
    }

    public Void execute(Context context) {
        GetDefaultValue sim = (GetDefaultValue) context.get( "simulator" );

        Object o;
        if ( this.contextName == null ) {
            o = context.get( this.identifier );
        } else {
            o = context.getContextManager().getContext( this.contextName ).get( this.identifier );
        }

        System.out.println( o );
        return null;
    }

}
