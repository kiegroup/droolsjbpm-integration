package org.drools.simulation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.drools.command.Command;
import org.drools.simulation.Path;
import org.drools.simulation.Step;

public class StepImpl
    implements
    Step {
    private Path                path;
    private Collection<Command> commands;
    private long                distance;

    public StepImpl(Path path,
                    Collection<Command> commands,
                    long distance) {
        this.path = path;
        this.commands = commands;
        this.distance = distance;
    }
    
    public StepImpl(Path path,
                    Command command,
                    long distance) {
        this.path = path;
        commands = new ArrayList<Command>();
        ((List<Command>)this.commands).add( command );
        this.distance = distance;
    }    

    public Collection<Command> getCommands() {
        return commands;
    }

    public long getTemporalDistance() {
        return distance;
    }

    public Path getPath() {
        return this.path;
    }

}
