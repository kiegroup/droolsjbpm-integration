package org.kie.remote.services.util;

import java.util.Map;

import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.jbpm.services.task.impl.model.xml.JaxbTask;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Task;
import org.kie.internal.command.Context;

public class ExecuteAndSerializeCommand extends TaskCommand<Object>{

    private TaskCommand command;

    public ExecuteAndSerializeCommand(){

    }

    public ExecuteAndSerializeCommand(TaskCommand command) {
        this.command = command;
    }

    @Override
    public Object execute(Context context) {
        Object cmdResult =  command.execute(context);
        if( cmdResult == null ) {
            return null;
        }
        if( cmdResult instanceof Task) {
            cmdResult = new JaxbTask((Task) cmdResult);
        } else if( cmdResult instanceof Content) {
            cmdResult = new JaxbContent((Content) cmdResult);
        } else if (cmdResult instanceof Map) {
            Map output = (Map) cmdResult;
            cmdResult = new JaxbContent();
            ((JaxbContent) cmdResult).setContentMap(output);
            ((JaxbContent) cmdResult).setId(-1L);
        }
        return cmdResult;
    }
}
