package org.kie.remote.services.util;

import java.util.List;

import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.command.Command;
import org.kie.internal.identity.IdentityProvider;
import org.kie.remote.services.AcceptedServerCommands;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.jaxb.JaxbCommandsRequest;
import org.kie.remote.services.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.rest.ExecuteResourceImpl;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecuteCommandUtil {

    protected static final Logger logger = LoggerFactory.getLogger(ExecuteResourceImpl.class);

    private ExecuteCommandUtil() {
        // util class
    }

    private static Boolean allowAllUsersAccessToAllTasks = Boolean.getBoolean("org.kie.task.insecure");

    public static JaxbCommandsResponse restProcessJaxbCommandsRequest(JaxbCommandsRequest request,
            IdentityProvider identityProvider, ProcessRequestBean processRequestBean) {
        // If exceptions are happening here, then there is something REALLY wrong and they should be thrown.
        JaxbCommandsResponse jaxbResponse = new JaxbCommandsResponse(request);
        List<Command> commands = request.getCommands();

        if (commands != null) {
            int cmdListSize = commands.size();

            // First check to make sure that all commands will be processed
            for (int i = 0; i < cmdListSize; ++i) {
                Command<?> cmd = commands.get(i);
                if (!AcceptedServerCommands.isAcceptedCommandClass(cmd.getClass()) ) {
                    throw KieRemoteRestOperationException.forbidden("The execute REST operation does not accept " + cmd.getClass().getName() + " instances.");
                }
                if( cmd instanceof TaskCommand ) {
                    String cmdName = cmd.getClass().getSimpleName();
                    if( ! allowAllUsersAccessToAllTasks ) {
                        String cmdUserId = ((TaskCommand) cmd).getUserId();
                        if( cmdUserId == null ) {
                            throw KieRemoteRestOperationException.badRequest("A null user id for a '" + cmdName + "' is not allowed!");
                        }
                        String authUserId = identityProvider.getName();
                        if( ! cmdUserId.equals(authUserId) ) {
                            throw KieRemoteRestOperationException.conflict("The user id used when retrieving task information (" + cmdUserId + ")"
                                    + " must match the authenticating user (" + authUserId + ")!");
                        }
                    }
                }
            }

            // Execute commands
            for (int i = 0; i < cmdListSize; ++i) {
                Command<?> cmd = commands.get(i);
                processRequestBean.processCommand(cmd, request, i, jaxbResponse);
            }
        }

        if (commands == null || commands.isEmpty()) {
            logger.info("Commands request object with no commands sent!");
        }

        return jaxbResponse;
    }


}
