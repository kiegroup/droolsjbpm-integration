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

import java.util.List;

import org.drools.core.command.impl.GenericCommand;
import org.kie.command.Command;
import org.kie.internal.command.Context;

public class TestGroupCommand
        implements GenericCommand<Void> {

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
