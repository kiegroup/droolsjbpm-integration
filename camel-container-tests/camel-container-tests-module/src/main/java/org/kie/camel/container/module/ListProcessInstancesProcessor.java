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

package org.kie.camel.container.module;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.process.ProcessInstance;

public class ListProcessInstancesProcessor implements Processor {

    private static final String OUT_ID = "out-identifier";

    @Override
    public void process(Exchange exchange) throws Exception {
        final ExecutionResults executionResults = (ExecutionResults) exchange.getIn().getBody();
        final List<ProcessInstance> processInstances = (List<ProcessInstance>) executionResults.getValue(OUT_ID);
        final List<Long> processInstancesIds = processInstances.stream()
                .map(x -> x.getId()).collect(Collectors.toList());

        exchange.getIn().setBody(processInstancesIds);
    }
}
