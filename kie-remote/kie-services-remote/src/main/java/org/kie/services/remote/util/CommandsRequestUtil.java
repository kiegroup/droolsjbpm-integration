package org.kie.services.remote.util;

import java.util.List;

import org.jboss.resteasy.spi.NotAcceptableException;
import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.command.Command;
import org.kie.services.client.api.command.AcceptedCommands;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbExceptionResponse;
import org.kie.services.remote.cdi.ProcessRequestBean;
import org.kie.services.remote.exception.KieRemoteServicesInternalError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandsRequestUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommandsRequestUtil.class);

    public static JaxbCommandsResponse restProcessJaxbCommandsRequest(JaxbCommandsRequest request, ProcessRequestBean requestBean) {
        // If exceptions are happening here, then there is something REALLY wrong and they should be thrown.
        JaxbCommandsResponse jaxbResponse = new JaxbCommandsResponse(request);
        List<Command<?>> commands = request.getCommands();

        if (commands != null) {
            for (int i = 0; i < commands.size(); ++i) {
                Command<?> cmd = commands.get(i);
                if (AcceptedCommands.getSet().contains(cmd.getClass())) {
                    throw new NotAcceptableException("The execute REST operation does not accept " + cmd.getClass().getSimpleName()
                            + " instances.");
                }
                logger.debug("Processing command " + cmd.getClass().getSimpleName());
                Object cmdResult = null;
                if (cmd instanceof TaskCommand<?>) {
                    cmdResult = requestBean.doTaskOperation(cmd);
                } else {
                    cmdResult = requestBean.doKieSessionOperation(cmd, request.getDeploymentId(), request.getProcessInstanceId());
                }
                if (cmdResult instanceof JaxbExceptionResponse) {
                    Exception e = ((JaxbExceptionResponse) cmdResult).cause;
                    if( e instanceof RuntimeException ) { 
                        throw (RuntimeException) e;
                    } else { 
                        throw new KieRemoteServicesInternalError("Unable to execute " + cmd.getClass().getSimpleName() + ": " 
                                + e.getMessage(), e);
                    }
                }
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

    public static JaxbCommandsResponse jmsProcessJaxbCommandsRequest(JaxbCommandsRequest request, ProcessRequestBean requestBean) {
        // If exceptions are happening here, then there is something REALLY wrong and they should be thrown.
        JaxbCommandsResponse jaxbResponse = new JaxbCommandsResponse(request);
        List<Command<?>> commands = request.getCommands();

        if (commands != null) {
            for (int i = 0; i < commands.size(); ++i) {
                Command<?> cmd = commands.get(i);
                if (AcceptedCommands.getSet().contains(cmd)) {
                    UnsupportedOperationException uoe = new UnsupportedOperationException(cmd.getClass().getSimpleName()
                            + " is not a supported command.");
                    jaxbResponse.addException(uoe, i, cmd);
                    continue;
                }

                Object cmdResult = null;
                if (cmd instanceof TaskCommand<?>) {
                    cmdResult = requestBean.doTaskOperation(cmd);
                } else {
                    cmdResult = requestBean.doKieSessionOperation(cmd, request.getDeploymentId(), request.getProcessInstanceId());
                }
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
