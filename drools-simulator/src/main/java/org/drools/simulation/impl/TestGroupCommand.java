package org.drools.simulation.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.drools.command.Command;
import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.reteoo.ReteooWorkingMemory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.AgendaFilter;
import org.mvel2.MVEL;

public class TestGroupCommand
    implements
    GenericCommand<Void> {

    private String        name;
    private List<Command> commands;

    public TestGroupCommand(String name,
                       List<Command> commands) {
        super();
        this.name = name;
        this.commands = commands;
    }

    public String getName() {
        return name;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public Void execute(Context context) {
        for ( Command command : commands ) {
            ((GenericCommand) command).execute( context );
        }
        return null;
    }

    public String toString() {
        return "test";
    }

}
