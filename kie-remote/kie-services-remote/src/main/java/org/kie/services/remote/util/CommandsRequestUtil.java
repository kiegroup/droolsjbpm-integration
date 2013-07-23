package org.kie.services.remote.util;

import java.util.List;

import org.jboss.resteasy.spi.BadRequestException;
import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.command.Command;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbCommandsResponse;
import org.kie.services.remote.cdi.ProcessRequestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandsRequestUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(CommandsRequestUtil.class);

    public static JaxbCommandsResponse processJaxbCommandsRequest(JaxbCommandsRequest request, ProcessRequestBean requestBean) {
        // If exceptions are happening here, then there is something REALLY wrong and they should be thrown.
        JaxbCommandsResponse jaxbResponse = new JaxbCommandsResponse(request);
        String deploymentId = request.getDeploymentId();
        Long processInstanceId = request.getProcessInstanceId();
        List<Command<?>> commands = request.getCommands();
        
        if( commands != null ) { 
            for (int i = 0; i < commands.size(); ++i) {
                Command<?> cmd = commands.get(i);
                logger.debug("Processing command " + cmd.getClass().getSimpleName() );
                boolean exceptionThrown = false;
                Object cmdResult = null;
                try {
                    if (cmd instanceof TaskCommand<?>) {
                        cmdResult = requestBean.doTaskOperation(cmd);
                    } else {
                        cmdResult = requestBean.doKieSessionOperation(cmd, deploymentId, processInstanceId);
                    }
                } catch (BadRequestException bre) {
                    logger.warn( "Unable to execute " + cmd.getClass().getSimpleName() + "/" + i + " because of " + bre.getClass().getSimpleName(), bre);
                    exceptionThrown = true;
                    throw bre;
                } catch (Exception e) {
                    exceptionThrown = true;
                    logger.warn( "Unable to execute " + cmd.getClass().getSimpleName() + "/" + i + " because of " + e.getClass().getSimpleName(), e);
                    jaxbResponse.addException(e, i, cmd);
                }
                if (!exceptionThrown) {
                    if (cmdResult != null) {
                        try {
                            // addResult could possibly throw an exception, which is why it's here and not above
                            jaxbResponse.addResult(cmdResult, i, cmd);
                        } catch (Exception e) {
                            logger.error("Unable to add result from " + cmd.getClass().getSimpleName() + "/" + i + " because of " + e.getClass().getSimpleName(), e);
                            jaxbResponse.addException(e, i, cmd);
                        }
                    }
                }
            }
        } 
        
        if( commands == null || commands.isEmpty() ) {
            logger.info( "Commands request object with no commands sent!" );
        }
        
        return jaxbResponse;
    }
}
