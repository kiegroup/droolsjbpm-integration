package org.kie.services.remote.util;

import java.util.HashSet;
import java.util.Set;

import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.ExitTaskCommand;
import org.jbpm.services.task.commands.FailTaskCommand;
import org.jbpm.services.task.commands.SkipTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;

public class Constants {

    public static Set<Class<? extends TaskCommand<?>>> TASK_COMMANDS_THAT_INFLUENCE_KIESESSION = new HashSet<Class<? extends TaskCommand<?>>>();
    static { 
        TASK_COMMANDS_THAT_INFLUENCE_KIESESSION.add(CompleteTaskCommand.class);
        TASK_COMMANDS_THAT_INFLUENCE_KIESESSION.add(ExitTaskCommand.class);
        TASK_COMMANDS_THAT_INFLUENCE_KIESESSION.add(FailTaskCommand.class);
        TASK_COMMANDS_THAT_INFLUENCE_KIESESSION.add(SkipTaskCommand.class);
    }
}
