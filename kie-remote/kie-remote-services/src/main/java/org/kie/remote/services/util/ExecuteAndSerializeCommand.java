package org.kie.remote.services.util;

import java.util.Map;

import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.jbpm.services.task.impl.model.xml.JaxbTask;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Task;
import org.kie.internal.command.Context;

public class ExecuteAndSerializeCommand<T> extends TaskCommand<T>{

    private TaskCommand<T> command;

    public ExecuteAndSerializeCommand() {

    }

    public ExecuteAndSerializeCommand(TaskCommand<T> command) {
        this.command = command;
    }

    @Override
    public T execute(Context context) {
        T cmdResult =  command.execute(context);
        if( cmdResult == null ) {
            return null;
        }
        if( cmdResult instanceof Task) {
            cmdResult = (T) new JaxbTask((Task) cmdResult);
        } else if( cmdResult instanceof Content) {
            cmdResult = (T) new JaxbContent((Content) cmdResult);
        } else if (cmdResult instanceof Map) {
            Map output = (Map) cmdResult;
            cmdResult = (T) new JaxbContent();
            ((JaxbContent) cmdResult).setContentMap(output);
            ((JaxbContent) cmdResult).setId(-1L);
        }
        return cmdResult;
    }
}
