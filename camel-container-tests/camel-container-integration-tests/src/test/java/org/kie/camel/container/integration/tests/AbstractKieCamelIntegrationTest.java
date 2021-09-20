/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.camel.container.integration.tests;

import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.WildcardTypePermission;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.drools.core.command.runtime.process.GetProcessInstancesCommand;
import org.drools.core.runtime.help.impl.BatchExecutionHelperProviderImpl;
import org.junit.Before;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.camel.container.api.service.KieCamelTestService;

public class AbstractKieCamelIntegrationTest {

    private static final String CAMEL_TEST_SERVICE_URL = "http://localhost:8080/rest";
    protected static final String DEFAULT_OUT_ID = "out-identifier";

    protected KieCamelTestService kieCamelTestService;
    protected KieCommands kieCommands;
    protected XStream xstreamMarshaller;

    @Before
    public void init() {
        kieCamelTestService = JAXRSClientFactory.create(CAMEL_TEST_SERVICE_URL, KieCamelTestService.class);

        kieCommands = KieServices.Factory.get().getCommands();

        final BatchExecutionHelperProviderImpl batchExecutionHelperProvider = new BatchExecutionHelperProviderImpl();
        xstreamMarshaller = batchExecutionHelperProvider.newXStreamMarshaller();
        String[] allowList = new String[]{
                                          "org.kie.camel.container.api.model.Person"
        };
        xstreamMarshaller.addPermission( new WildcardTypePermission( allowList ) );
    }

    protected ExecutionResults runCommand(Command command) {
        final String commandXML = xstreamMarshaller.toXML(command);
        final String resultsXML = kieCamelTestService.runCommand(commandXML);
        final ExecutionResults executionResults = (ExecutionResults) xstreamMarshaller.fromXML(resultsXML);

        return executionResults;
    }

    protected List<Long> listProcesses() {
        final GetProcessInstancesCommand getProcessInstancesCommand = new GetProcessInstancesCommand();
        getProcessInstancesCommand.setOutIdentifier(DEFAULT_OUT_ID);
        final String commandXML = xstreamMarshaller.toXML(getProcessInstancesCommand);
        final String resultsXML = kieCamelTestService.runCommand(commandXML);

        final List results = (List) xstreamMarshaller.fromXML(resultsXML);
        final List<Long> processIds = (List<Long>) results;
        return processIds;
    }
}
