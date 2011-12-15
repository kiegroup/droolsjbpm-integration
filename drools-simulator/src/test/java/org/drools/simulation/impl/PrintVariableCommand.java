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