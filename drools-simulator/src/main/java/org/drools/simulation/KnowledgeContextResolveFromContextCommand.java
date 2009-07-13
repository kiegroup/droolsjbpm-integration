package org.drools.simulation;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.command.Command;
import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.runtime.StatefulKnowledgeSession;

public class KnowledgeContextResolveFromContextCommand
    implements
    GenericCommand {

    private String  kbaseIdentifier;
    private String  kbuilderIdentifier;
    private String  statefulKsessionName;
    private Command command;

    public KnowledgeContextResolveFromContextCommand(Command command,
                                                     String kbuilderIdentifier,
                                                     String kbaseIdentifier,
                                                     String statefulKsessionName) {
        this.command = command;
        this.kbuilderIdentifier = kbuilderIdentifier;
        this.kbaseIdentifier = kbaseIdentifier;
        this.statefulKsessionName = statefulKsessionName;
    }

    public Object execute(Context context) {
        KnowledgeCommandContext kcContext = new KnowledgeCommandContext( context,
                                                                   (KnowledgeBuilder) context.get( this.kbuilderIdentifier ),
                                                                   (KnowledgeBase) context.get( this.kbaseIdentifier ),
                                                                   (StatefulKnowledgeSession) context.get( this.statefulKsessionName ));
        return ((GenericCommand)command).execute( kcContext );
    }
    
    public Command getCommand() {
        return this.command;
    }

}
