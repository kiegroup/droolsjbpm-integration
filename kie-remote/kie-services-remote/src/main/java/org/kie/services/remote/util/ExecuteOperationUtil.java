package org.kie.services.remote.util;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.jboss.resteasy.spi.NotAcceptableException;
import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.command.Command;
import org.kie.services.client.api.command.AcceptedCommands;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbExceptionResponse;
import org.kie.services.remote.exception.KieRemoteServicesInternalError;
import org.kie.services.remote.request.AsyncSessionRequestEjBean;
import org.kie.services.remote.request.RequestBean;
import org.kie.services.remote.request.TaskRequestCdiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecuteOperationUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteOperationUtil.class);

    public static JaxbCommandsResponse restProcessJaxbCommandsRequest(JaxbCommandsRequest request, RequestBean requestBean) {
        // If exceptions are happening here, then there is something REALLY wrong and they should be thrown.
        JaxbCommandsResponse jaxbResponse = new JaxbCommandsResponse(request);
        List<Command<?>> commands = request.getCommands();

        if (commands != null) {
            // Loop through all commands
            for (int i = 0; i < commands.size(); ++i) {
                Command<?> cmd = commands.get(i);
                
                // Check if the command is accepted
                if (!AcceptedCommands.getSet().contains(cmd.getClass())) {
                    throw new NotAcceptableException("The execute REST operation does not accept " + cmd.getClass().getName()
                            + " instances.");
                }
                
                // Process command
                logger.debug("Processing command " + cmd.getClass().getSimpleName());
                Object cmdResult = null;
                if (cmd instanceof TaskCommand<?>) {
                    cmdResult = ((TaskRequestCdiBean) requestBean).doTaskOperation(cmd);
                } else {
                    Future<Object> futureResult 
                        = ((AsyncSessionRequestEjBean) requestBean).doKieSessionOperation(
                                cmd, request.getDeploymentId(), request.getProcessInstanceId());
                    try {
                        cmdResult = futureResult.get();
                    } catch (Exception e) {
                        JaxbExceptionResponse exceptResp = new JaxbExceptionResponse(e, cmd);
                        logger.warn( "Unable to execute " + exceptResp.getCommandName() + " because of " + e.getClass().getSimpleName() + ": " + e.getMessage());
                        logger.trace("Stack trace: \n", e);
                        cmdResult = exceptResp;
                    }
                }
                
                // If processing the command has caused an exception, throw the exception (result)
                if (cmdResult instanceof JaxbExceptionResponse) {
                    Exception e = ((JaxbExceptionResponse) cmdResult).cause;
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    } else {
                        throw new KieRemoteServicesInternalError("Unable to execute " + cmd.getClass().getSimpleName() + ": " + e.getMessage(), e);
                    }
                }
               
                // Add the result of the command to the (JAXB) response object
                if (cmdResult != null) {
                    try {
                        // addResult could possibly throw an exception, which is why it's here and not above
                        jaxbResponse.addResult(cmdResult, i, cmd);
                    } catch (Exception e) {
                        logger.error("Unable to add result from " + cmd.getClass().getSimpleName() + "/" + i + " because of "
                                + e.getClass().getSimpleName(), e);
                        jaxbResponse.addException(e, i, cmd);
                    }
                }
            }
        }

        if (commands == null || commands.isEmpty()) {
            logger.info("Commands request object with no commands sent!");
        }

        return jaxbResponse;
    }


}
