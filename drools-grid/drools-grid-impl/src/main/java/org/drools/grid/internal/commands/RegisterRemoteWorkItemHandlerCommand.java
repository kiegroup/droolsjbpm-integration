package org.drools.grid.internal.commands;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.kie.command.Context;
import org.kie.runtime.KnowledgeRuntime;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.WorkItemHandler;

public class RegisterRemoteWorkItemHandlerCommand
    implements
    GenericCommand<Object> {

    private String handler;
    private String workItemName;

    public RegisterRemoteWorkItemHandlerCommand() {
    }

    public RegisterRemoteWorkItemHandlerCommand(String workItemName,
                                                String handler) {
        this.handler = handler;
        this.workItemName = workItemName;
    }

    public String getHandler() {
        return this.handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getWorkItemName() {
        return this.workItemName;
    }

    public void setWorkItemName(String workItemName) {
        this.workItemName = workItemName;
    }

    public Object execute(Context context) {
        StatefulKnowledgeSession ksession = ((KnowledgeCommandContext) context).getStatefulKnowledgesession();
        WorkItemHandler workItemHandler = null;
        try {
            Class t = Class.forName( this.handler );
            Constructor c = t.getConstructor( KnowledgeRuntime.class );
            workItemHandler = (WorkItemHandler) c.newInstance( ksession );
        } catch ( InstantiationException ex ) {
            Logger.getLogger( RegisterRemoteWorkItemHandlerCommand.class.getName() ).log( Level.SEVERE,
                                                                                          null,
                                                                                          ex );
        } catch ( IllegalAccessException ex ) {
            Logger.getLogger( RegisterRemoteWorkItemHandlerCommand.class.getName() ).log( Level.SEVERE,
                                                                                          null,
                                                                                          ex );
        } catch ( IllegalArgumentException ex ) {
            Logger.getLogger( RegisterRemoteWorkItemHandlerCommand.class.getName() ).log( Level.SEVERE,
                                                                                          null,
                                                                                          ex );
        } catch ( InvocationTargetException ex ) {
            Logger.getLogger( RegisterRemoteWorkItemHandlerCommand.class.getName() ).log( Level.SEVERE,
                                                                                          null,
                                                                                          ex );
        } catch ( NoSuchMethodException ex ) {
            Logger.getLogger( RegisterRemoteWorkItemHandlerCommand.class.getName() ).log( Level.SEVERE,
                                                                                          null,
                                                                                          ex );
        } catch ( SecurityException ex ) {
            Logger.getLogger( RegisterRemoteWorkItemHandlerCommand.class.getName() ).log( Level.SEVERE,
                                                                                          null,
                                                                                          ex );
        } catch ( ClassNotFoundException ex ) {
            Logger.getLogger( RegisterRemoteWorkItemHandlerCommand.class.getName() ).log( Level.SEVERE,
                                                                                          null,
                                                                                          ex );
        }
        ksession.getWorkItemManager().registerWorkItemHandler( this.workItemName,
                                                               workItemHandler );
        return null;
    }

    @Override
    public String toString() {
        return "session.getWorkItemManager().registerWorkItemHandler("
               + this.workItemName + ", " + this.handler + ");";
    }

}