package org.kie.services.client.serialization.jaxb;

import java.util.ArrayList;
import java.util.List;

import org.drools.core.command.runtime.process.StartProcessCommand;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbRequestStatus;
import org.yaml.snakeyaml.Yaml;

public class YamlSerializationTest {

    @Test
    public void genericTest() {
        JaxbGenericResponse resp = new JaxbGenericResponse();
        resp.setError("error");
        resp.setStackTrace("stack");
        resp.setStatus(JaxbRequestStatus.SUCCESS);
        resp.setUrl("http://here");

        Yaml yaml = new Yaml();
        String output = yaml.dump(resp);
        Object reqObj = yaml.load(output);
    }
    
    @Test
    public void commandsRequestTest() {
        JaxbCommandsRequest req = new JaxbCommandsRequest();
        List<Command<?>> cmds = new ArrayList<Command<?>>();
        req.setCommands(cmds);
        req.setDeploymentId("depId");
        req.setProcessInstanceId(43l);
        req.setVersion(2);
        cmds.add(new StartProcessCommand("test.proc.yaml"));

        Yaml yaml = new Yaml();
        String output = yaml.dump(req);
        Object reqObj = yaml.load(output);
    }
    
}
