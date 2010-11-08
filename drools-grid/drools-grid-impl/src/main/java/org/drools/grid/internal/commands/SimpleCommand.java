package org.drools.grid.internal.commands;

import java.io.Serializable;
import java.util.List;

public class SimpleCommand
    implements
    Serializable {

    private int               id;

    private SimpleCommandName name;

    private List<Object>      arguments;

    public SimpleCommand(int id,
                         SimpleCommandName name,
                         List<Object> arguments) {
        super();
        this.id = id;
        this.arguments = arguments;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SimpleCommandName getName() {
        return this.name;
    }

    public void setName(SimpleCommandName name) {
        this.name = name;
    }

    public List<Object> getArguments() {
        return this.arguments;
    }

    public void setArguments(List<Object> arguments) {
        this.arguments = arguments;
    }

}
