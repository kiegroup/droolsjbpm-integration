package org.kie.server.services.drools.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.command.Command;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.CreateContainerCommand;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.commands.GetContainerInfoCommand;
import org.kie.server.api.commands.GetScannerInfoCommand;
import org.kie.server.api.commands.GetServerInfoCommand;
import org.kie.server.api.commands.ListContainersCommand;
import org.kie.server.api.commands.RegisterServerControllerCommand;
import org.kie.server.api.commands.UpdateReleaseIdCommand;
import org.kie.server.api.commands.UpdateScannerCommand;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.services.api.KieContainerExecutor;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.drools.RuleService;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.KieServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleServiceImpl implements RuleService, KieContainerExecutor {

    private static final Pattern LOOKUP               = Pattern.compile("[\"']?lookup[\"']?\\s*[:=]\\s*[\"']([^\"']+)[\"']");
    private static final Logger logger               = LoggerFactory.getLogger(RuleServiceImpl.class);

    private KieServerImpl kieServer;
    private final KieServerRegistry context;

    public RuleServiceImpl(KieServerImpl kieServer, KieServerRegistry context) {
        this.kieServer = kieServer;
        this.context = context;
    }

    @Override
    public ServiceResponse<String> callContainer(String containerId, String payload) {
        if( payload == null ) {
            return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Error calling container " + containerId + ". Empty payload. ");
        }
        try {
            KieContainerInstanceImpl kci = (KieContainerInstanceImpl) context.getContainer( containerId );
            // the following code is subject to a concurrent call to dispose(), but the cost of synchronizing it
            // would likely not be worth it. At this point a decision was made to fail the execution if a concurrent
            // call do dispose() is executed.
            if (kci != null && kci.getKieContainer() != null) {
                String sessionId = null;
                // this is a weak way of finding the lookup, but it is the same used in kie-camel. Will keep it for now.
                Matcher m = LOOKUP.matcher(payload);
                if (m.find()) {
                    sessionId = m.group(1);
                }

                // find the session
                CommandExecutor ks = null;
                if( sessionId != null ) {
                    KieSessionModel ksm = kci.getKieContainer().getKieSessionModel(sessionId);
                    if( ksm != null ) {
                        switch (ksm.getType() ) {
                            case STATEFUL:
                                ks = kci.getKieContainer().getKieSession(sessionId);
                                break;
                            case STATELESS:
                                ks = kci.getKieContainer().getStatelessKieSession(sessionId);
                                break;
                        }
                    }
                } else {
                    // if no session ID is defined, then the default is a stateful session
                    ks = kci.getKieContainer().getKieSession();
                }
                if (ks != null) {
                    Command<?> cmd = kci.getMarshaller( MarshallingFormat.XSTREAM ).unmarshall(payload, Command.class);

                    if (cmd == null) {
                        return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Body of in message not of the expected type '" + Command.class.getName() + "'");
                    }
                    if (!(cmd instanceof BatchExecutionCommandImpl)) {
                        cmd = new BatchExecutionCommandImpl(Arrays.asList(new GenericCommand<?>[]{(GenericCommand<?>) cmd}));
                    }

                    ExecutionResults results = ks.execute((BatchExecutionCommandImpl) cmd);
                    String result = kci.getMarshaller( MarshallingFormat.XSTREAM ).marshall(results);
                    return new ServiceResponse<String>(ServiceResponse.ResponseType.SUCCESS, "Container " + containerId + " successfully called.", result);
                } else {
                    return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Session '" + sessionId + "' not found on container '" + containerId + "'.");
                }
            } else {
                return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Container " + containerId + " is not instantiated.");
            }
        } catch (Exception e) {
            logger.error("Error calling container '" + containerId + "'", e);
            return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Error calling container " + containerId + ": " +
                    e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public ServiceResponsesList executeScript(CommandScript commands) {
        List<ServiceResponse<? extends Object>> responses = new ArrayList<ServiceResponse<? extends Object>>();
        if( commands != null ) {
            for (KieServerCommand command : commands.getCommands()) {
                if (command instanceof CreateContainerCommand) {
                    responses.add(this.kieServer.createContainer(((CreateContainerCommand) command).getContainer().getContainerId(), ((CreateContainerCommand) command).getContainer()));
                } else if (command instanceof GetServerInfoCommand) {
                    responses.add(this.kieServer.getInfo());
                } else if (command instanceof ListContainersCommand) {
                    responses.add(this.kieServer.listContainers());
                } else if (command instanceof CallContainerCommand) {
                    responses.add(callContainer(((CallContainerCommand) command).getContainerId(), ((CallContainerCommand) command).getPayload()));
                } else if (command instanceof DisposeContainerCommand) {
                    responses.add(this.kieServer.disposeContainer(((DisposeContainerCommand) command).getContainerId()));
                } else if (command instanceof GetContainerInfoCommand) {
                    responses.add(this.kieServer.getContainerInfo(((GetContainerInfoCommand) command).getContainerId()));
                } else if (command instanceof GetScannerInfoCommand) {
                    responses.add(this.kieServer.getScannerInfo(((GetScannerInfoCommand) command).getContainerId()));
                } else if (command instanceof UpdateScannerCommand) {
                    responses.add(this.kieServer.updateScanner(((UpdateScannerCommand) command).getContainerId(), ((UpdateScannerCommand) command).getScanner()));
                } else if (command instanceof UpdateReleaseIdCommand) {
                    responses.add(this.kieServer.updateContainerReleaseId(((UpdateReleaseIdCommand) command).getContainerId(), ((UpdateReleaseIdCommand) command).getReleaseId()));
                } else if (command instanceof RegisterServerControllerCommand) {
                    responses.add(this.kieServer.registerController(((RegisterServerControllerCommand) command).getControllerUrl(), ((RegisterServerControllerCommand) command).getKieServerConfig()));
                }
            }
        }
        return new ServiceResponsesList(responses);
    }
}
