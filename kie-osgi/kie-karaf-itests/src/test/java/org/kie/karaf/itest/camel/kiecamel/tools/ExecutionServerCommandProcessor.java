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

package org.kie.karaf.itest.camel.kiecamel.tools;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.kie.camel.KieCamelUtils;

public class ExecutionServerCommandProcessor implements Processor {

    private static final String CLIENT_HEADDER = "CamelKieClient";
    private static final String OPERATION_HEADER = "CamelKieOperation";
    private static final String BODY_PARAM_HEADER = "CamelKieBodyParam";

    @Override
    public void process(Exchange exchange) throws Exception {
        final ExecutionServerCommand command = (ExecutionServerCommand) exchange.getIn().getBody();

        exchange.getIn().setHeader(CLIENT_HEADDER, command.getClient());
        exchange.getIn().setHeader(OPERATION_HEADER, command.getOperation());
        exchange.getIn().setHeader(BODY_PARAM_HEADER, command.getBodyParam());

        for (String key : command.getParameters().keySet()) {
            exchange.getIn().setHeader(KieCamelUtils.asCamelKieName(key), command.getParameters().get(key));
        }

        exchange.getIn().setBody(command.getBody());
    }
}
