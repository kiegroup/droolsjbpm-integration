package org.drools.camel.example;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;

import java.util.List;

public class FireAllCommands {

    public void generateCommand(Exchange exchange) throws Exception {
        System.out.println(">> We will fire all rules commands");
        FireAllRulesCommand fireAllRulesCommand = new FireAllRulesCommand();
        exchange.getIn().setBody(fireAllRulesCommand);
    }

    public void insertAndFireAll(Exchange exchange) {
        final Message in = exchange.getIn();
        final Object body = in.getBody();

        BatchExecutionCommandImpl command = new BatchExecutionCommandImpl();
        final List<GenericCommand<?>> commands = command.getCommands();
        commands.add(new InsertObjectCommand(body, "obj1"));
        commands.add(new FireAllRulesCommand());

        in.setBody(command);
    }
}
