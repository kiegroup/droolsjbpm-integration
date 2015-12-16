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

package org.kie.remote.services.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.drools.core.command.runtime.process.StartProcessCommand;
import org.jbpm.services.task.commands.GetTaskAssignedAsBusinessAdminCommand;
import org.jbpm.services.task.commands.GetTasksByProcessInstanceIdCommand;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class VersionedJaxbCommandsRequestResponseTest {

    protected static final Logger logger = LoggerFactory.getLogger(VersionedJaxbCommandsRequestResponseTest.class);
    
    protected JaxbSerializationProvider jaxbProvider = ServerJaxbSerializationProvider.newInstance();
    { 
        jaxbProvider.setPrettyPrint(true);
    }

    public <T> T testRoundTrip(T in) throws Exception {
        String xmlStr = jaxbProvider.serialize(in);
        logger.debug(xmlStr);
        return (T) deserialize(xmlStr);
    }
    
    public Object deserialize(String xmlStr) { 
        return jaxbProvider.deserialize(xmlStr);
    }

   
    String cmdReqXmlStr = "<command-request>"
            + "    <deployment-id>depId</deployment-id>"
            + "    <process-instance-id>43</process-instance-id>"
            + "    <ver>2</ver>"
            + "    <user>krampus</user>"
            + "    <start-process processId=\"test.proc.yaml\">"
            + "        <parameter>"
            + "            <item key=\"two\">"
            + "                <value xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">B</value>"
            + "            </item>"
            + "            <item key=\"thr\">"
            + "                <value xsi:type=\"ns3:intArray\" xmlns:ns3=\"http://jaxb.dev.java.net/array\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
            + "                    <item>59</item>"
            + "                    <item>2195</item>"
            + "                </value>"
            + "            </item>"
            + "            <item key=\"one\">"
            + "                <value xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">a</value>"
            + "            </item>"
            + "        </parameter>"
            + "    </start-process>"
            + "</command-request>";

        
    @Test
    public void commandRequestTest() throws Exception {
        JaxbCommandsRequest req = new JaxbCommandsRequest();
        req.setUser("krampus");
        List<Command> cmds = new ArrayList<Command>();
        req.setCommands(cmds);
        req.setDeploymentId("depId");
        req.setProcessInstanceId(43l);
        req.setVersion("2");
        StartProcessCommand spCmd = new StartProcessCommand("test.proc.yaml");
        cmds.add(spCmd);
        spCmd.getParameters().put("one", "a");
        spCmd.getParameters().put("two", "B");
        Object weirdParam = new Integer[] { 59, 2195 };
        spCmd.getParameters().put("thr", weirdParam);
        
        JaxbCommandsRequest newReq = testRoundTrip(req);
        assertEquals(((StartProcessCommand) newReq.getCommands().get(0)).getParameters().get("two"), "B");
        
        Object newStringReq = deserialize(cmdReqXmlStr);
        assertNotNull(newStringReq);
        assertEquals( 1, ((JaxbCommandsRequest) newStringReq).getCommands().size());
        Command<?> newCmd = ((JaxbCommandsRequest) newStringReq).getCommands().get(0);
        assertNotNull( newCmd );
    }
   
    String cmdRespXmlStr = "<command-response>"
            + "    <ver>1</ver>"
            + "    <task-summary-list index=\"0\">"
            + "        <command-name>GetTaskAssignedAsBusinessAdminCommand</command-name>"
            + "    </task-summary-list>"
            + "    <long-list index=\"1\">"
            + "        <command-name>GetTasksByProcessInstanceIdCommand</command-name>"
            + "    </long-list>"
            + "</command-response>";

    @Test
    public void commandsResponseTest() throws Exception {
        Command<?> cmd = new GetTaskAssignedAsBusinessAdminCommand();
        List<TaskSummary> result = new ArrayList<TaskSummary>();

        JaxbCommandsResponse resp = new JaxbCommandsResponse();
        resp.addResult(result, 0, cmd);

        cmd = new GetTasksByProcessInstanceIdCommand();
        List<Long> resultTwo = new ArrayList<Long>();
        resp.addResult(resultTwo, 1, cmd);

        Object newResp = testRoundTrip(resp);
        assertNotNull(newResp);
        assertEquals( 2, ((JaxbCommandsResponse) newResp).getResponses().size());
        
        Object newStringResp = deserialize(cmdRespXmlStr);
        assertNotNull(newStringResp);
        assertEquals( 2, ((JaxbCommandsResponse) newStringResp).getResponses().size());
        List<JaxbCommandResponse<?>> respList = ((JaxbCommandsResponse) newStringResp).getResponses();
        assertNotNull( respList );
        assertFalse( respList.isEmpty() );
    }
}
