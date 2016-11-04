/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.services.impl;

import org.drools.core.command.impl.ExecutableCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
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
import org.kie.server.api.commands.GetServerStateCommand;
import org.kie.server.api.commands.ListContainersCommand;
import org.kie.server.api.commands.UpdateReleaseIdCommand;
import org.kie.server.api.commands.UpdateScannerCommand;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.locator.ContainerLocatorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KieContainerCommandServiceImpl implements KieContainerCommandService<ExecutionResults> {

    protected static final Pattern LOOKUP            = Pattern.compile("[\"']?lookup[\"']?\\s*[:=]\\s*[\"']([^\"']+)[\"']");
    private static final Logger logger             = LoggerFactory.getLogger(KieContainerCommandServiceImpl.class);

    protected KieServerImpl kieServer;
    protected final KieServerRegistry context;

    public KieContainerCommandServiceImpl(KieServerImpl kieServer, KieServerRegistry context) {
        this.kieServer = kieServer;
        this.context = context;
    }

    public ServiceResponse<ExecutionResults> callContainer(String containerId, String payload, MarshallingFormat marshallingFormat, String classType) {
        return callContainer(containerId, payload, marshallingFormat, classType, false);
    }

    protected ServiceResponse<ExecutionResults> callContainer(String containerId, String payload, MarshallingFormat marshallingFormat, String classType, boolean marshallResponse) {
        if( payload == null ) {
            return new ServiceResponse<ExecutionResults>(ServiceResponse.ResponseType.FAILURE, "Error calling container " + containerId + ". Empty payload. ");
        }
        try {
            KieContainerInstanceImpl kci = (KieContainerInstanceImpl) context.getContainer( containerId, ContainerLocatorProvider.get().getLocator() );
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
                    ks = context.getKieSessionLookupManager().lookup(sessionId, kci, context);
                } else {
                    // if no session ID is defined, then the default is a stateful session
                    ks = kci.getKieContainer().getKieSession();
                }
                if (ks != null) {
                    Class<? extends Command> type =  BatchExecutionCommandImpl.class;
                    if (classType != null && !classType.isEmpty()) {
                        type = (Class<? extends Command>) kci.getKieContainer().getClassLoader().loadClass(classType);
                    }

                    Command<?> cmd = kci.getMarshaller( marshallingFormat ).unmarshall(payload, type);

                    if (cmd == null) {
                        return new ServiceResponse<ExecutionResults>(ServiceResponse.ResponseType.FAILURE, "Body of in message not of the expected type '" + Command.class.getName() + "'");
                    }
                    if (!(cmd instanceof BatchExecutionCommandImpl)) {
                        cmd = new BatchExecutionCommandImpl(Arrays.asList(new ExecutableCommand<?>[]{(ExecutableCommand<?>) cmd} ));
                    }

                    ExecutionResults results = ks.execute((BatchExecutionCommandImpl) cmd);
                    if (marshallResponse) {
                        Marshaller marshaller = kci.getMarshaller(marshallingFormat);
                        String result = marshaller.marshall(results);

                        return new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "Container " + containerId + " successfully called.", result);
                    } else {

                        return new ServiceResponse<ExecutionResults>(ServiceResponse.ResponseType.SUCCESS, "Container " + containerId + " successfully called.", results);
                    }
                } else {
                    return new ServiceResponse<ExecutionResults>(ServiceResponse.ResponseType.FAILURE, "Session '" + sessionId + "' not found on container '" + containerId + "'.");
                }
            } else {
                return new ServiceResponse<ExecutionResults>(ServiceResponse.ResponseType.FAILURE, "Container " + containerId + " is not instantiated.");
            }
        } catch (Exception e) {
            logger.error("Error calling container '" + containerId + "'", e);
            return new ServiceResponse<ExecutionResults>(ServiceResponse.ResponseType.FAILURE, "Error calling container " + containerId + ": " +
                    e.getClass().getName() + ": " + e.getMessage());
        }
    }

    @Override
    public ServiceResponsesList executeScript(CommandScript commands, MarshallingFormat marshallingFormat, String classType) {
        List<ServiceResponse<? extends Object>> responses = new ArrayList<ServiceResponse<? extends Object>>();
        if( commands != null ) {
            for (KieServerCommand command : commands.getCommands()) {
                if (command instanceof CreateContainerCommand) {
                    responses.add(this.kieServer.createContainer(((CreateContainerCommand) command).getContainer().getContainerId(), ((CreateContainerCommand) command).getContainer()));
                } else if (command instanceof GetServerInfoCommand) {
                    responses.add(this.kieServer.getInfo());
                } else if (command instanceof ListContainersCommand) {
                    responses.add(this.kieServer.listContainers(((ListContainersCommand)command).getKieContainerResourceFilter()));
                } else if (command instanceof CallContainerCommand) {
                    ServiceResponse response = callContainer(((CallContainerCommand) command).getContainerId(), ((CallContainerCommand) command).getPayload(), marshallingFormat, classType, true);

                    responses.add(response);
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
                } else if (command instanceof GetServerStateCommand) {
                    responses.add(this.kieServer.getServerState());
                }
            }
        }
        return new ServiceResponsesList(responses);
    }
}
