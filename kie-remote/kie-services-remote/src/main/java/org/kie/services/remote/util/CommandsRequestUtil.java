package org.kie.services.remote.util;

import java.util.List;

import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.command.Command;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbCommandsResponse;
import org.kie.services.remote.cdi.ProcessRequestBean;

public class CommandsRequestUtil {

    public static JaxbCommandsResponse processJaxbCommandsRequest(JaxbCommandsRequest request, ProcessRequestBean requestBean) {
        JaxbCommandsResponse jaxbResponse = new JaxbCommandsResponse(request);
        try {
            String deploymentId = request.getDeploymentId();
            Long processInstanceId = request.getProcessInstanceId();
            List<Command<?>> commands = request.getCommands();
            for (int i = 0; i < commands.size(); ++i) {
                Command<?> cmd = commands.get(i);
                boolean exceptionThrown = false;
                Object cmdResult = null;
                try {
                    if (cmd instanceof TaskCommand<?>) {
                        cmdResult = requestBean.doTaskOperation(cmd);
                    } else {
                        cmdResult = requestBean.doKieSessionOperation(cmd, deploymentId, processInstanceId);
                    }
                } catch (Exception e) {
                    // TODO: log exception
                    e.printStackTrace();
                    jaxbResponse.addException(e, i, cmd);
                }
                if (!exceptionThrown) {
                    if (cmdResult != null) {
                        try {
                            // addResult could possibly throw an exception, which is why it's here and not above
                            jaxbResponse.addResult(cmdResult, i, cmd);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Error executing command " + request, t);
        }
        return jaxbResponse;
    }
}
