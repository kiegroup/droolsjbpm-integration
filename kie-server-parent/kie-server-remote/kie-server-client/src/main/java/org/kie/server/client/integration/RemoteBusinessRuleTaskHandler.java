/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.client.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.internal.runtime.Cacheable;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.DMNServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.RuleServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Work item handler to support remote execution of rules via KIE Server. <br/>
 * Supports both DRL and DMN that needs to be specified via Language data intput - defaults to DRL.
 *
 * Following is a list of supported data inputs:
 * <ul>
 *     <li>Language - DRL or DMN (optional and defaults to DRL)</li>
 *     <li>ContainerId - container id (or alias) to be targeted on remote KIE Server - mandatory</li>
 * </ul>
 *
 * Following are data inputs specific to DRL:
 * <ul>
 *     <li>KieSessionName - name of the kie session to be used on remote KIE Server - optional</li>
 * </ul>
 * Following are data inputs specific to DMN:
 * <ul>
 *     <li>Namespace - DMN namespace to be used - mandatory</li>
 *     <li>Model - DMN model to be used - mandatory</li>
 *     <li>Decision - DMN decision name to be used - optional</li>
 * </ul>
 *
 * All other data inputs will be used as facts inserted into decision service.<br/>
 * Results returned will be then put back into the data outputs. <br/>
 * <br/>
 * DRL handling is based on same names for data input and output as that is then used as correlation.<br/>
 * DMN handling receives all data from DMNResult.<br/>
 */
public class RemoteBusinessRuleTaskHandler implements Cacheable, WorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(RemoteBusinessRuleTaskHandler.class);

    protected static final String DRL_LANG = "DRL";
    protected static final String DMN_LANG = "DMN";

    protected KieCommands commandsFactory = KieServices.get().getCommands();
    private KieServicesClient client;

    public RemoteBusinessRuleTaskHandler(String serverUrl, String userName, String password, ClassLoader classLoader) {
        // expand from system property if given otherwise use the same value
        serverUrl = System.getProperty(serverUrl, serverUrl);
        logger.debug("KieServerClient configured for server url(s) {} and username {}", serverUrl, userName);
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(serverUrl, userName, password);
        configuration.setMarshallingFormat(MarshallingFormat.XSTREAM);
        this.client =  KieServicesFactory.newKieServicesClient(configuration, classLoader);
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager workItemManager) {
        Map<String, Object> parameters = new HashMap<>(workItem.getParameters());
        String containerId = (String) parameters.remove("ContainerId");
        if (containerId == null || containerId.isEmpty()) {
            throw new IllegalArgumentException("Container ID is required for remote BusinessRuleTask");
        }

        String language = (String) parameters.remove("Language");
        if (language == null) {
            language = DRL_LANG;
        }
        String kieSessionName = (String) parameters.remove("KieSessionName");

        // remove engine specific parameters
        parameters.remove("TaskName");
        parameters.remove("KieSessionType");

        Map<String, Object> results = new HashMap<>();

        logger.debug("Facts to be inserted into working memory {}", parameters);
        if (DRL_LANG.equalsIgnoreCase(language)) {

            RuleServicesClient ruleClient = client.getServicesClient(RuleServicesClient.class);

            List<Command<?>> commands = new ArrayList<Command<?>>();
            BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, kieSessionName);

            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String inputKey = workItem.getId() + "_" + entry.getKey();
                commands.add(commandsFactory.newInsert(entry.getValue(), inputKey, true, null));
            }
            commands.add(commandsFactory.newFireAllRules("Fired"));

            ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(containerId, executionCommand);
            if (ServiceResponse.ResponseType.FAILURE.equals(reply.getType())) {
                throw new KieServicesException(reply.getMsg());
            }
            ExecutionResults executionResults = reply.getResult();

            logger.debug("{} rules fired", executionResults.getValue("Fired"));

            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String inputKey = workItem.getId() + "_" + entry.getKey();
                String key = entry.getKey().replaceAll(workItem.getId() + "_", "");
                results.put(key, executionResults.getValue(inputKey));
            }

        } else if (DMN_LANG.equalsIgnoreCase(language)) {
            String namespace = (String) parameters.remove("Namespace");
            String model = (String) parameters.remove("Model");
            String decision = (String) parameters.remove("Decision");
            DMNServicesClient dmnClient = client.getServicesClient(DMNServicesClient.class);

            DMNContext dmnContext = dmnClient.newContext();

            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                dmnContext.set(entry.getKey(), entry.getValue());
            }

            ServiceResponse<DMNResult> evaluationResult = null;
            if (decision != null) {
                evaluationResult = dmnClient.evaluateDecisionByName(containerId, namespace, model, decision, dmnContext);
            } else {
                evaluationResult = dmnClient.evaluateAll(containerId, namespace, model, dmnContext);
            }

            DMNResult dmnResult = evaluationResult.getResult();

            results.putAll(dmnResult.getContext().getAll());
        } else {
            throw new IllegalArgumentException("Not supported language type " + language);
        }
        logger.debug("Facts retrieved from working memory {}", results);
        workItemManager.completeWorkItem(workItem.getId(), results);

    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager workItemManager) {
        // no-op
    }

    @Override
    public void close() {

    }

}
