package org.kie.camel.container.module;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.kie.camel.KieCamelUtils;
import org.kie.camel.container.api.ExecutionServerCommand;

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
