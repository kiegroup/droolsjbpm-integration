package org.kie.remote.client.jaxb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.jbpm.services.task.impl.model.xml.JaxbI18NText;
import org.jbpm.services.task.impl.model.xml.JaxbTask;
import org.jbpm.services.task.impl.model.xml.JaxbTaskData;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Test;
import org.kie.api.task.model.I18NText;
import org.kie.remote.jaxb.gen.Task;
import org.kie.services.client.serialization.JsonSerializationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonRemoteClientSerializationTest extends JbpmJUnitBaseTestCase {

    protected static final Logger logger = LoggerFactory.getLogger(JsonRemoteClientSerializationTest.class); 

    protected JsonSerializationProvider jsonProvider = new JsonSerializationProvider();
    protected ObjectMapper objectMapper = new ObjectMapper();

    public <T> T testRoundTrip(T in) throws Exception {
        String xmlObject = jsonProvider.serialize(in);
        logger.debug(xmlObject);
        return (T) jsonProvider.deserialize(xmlObject, in.getClass());
    }
   
    @Test
    public void jsonTaskStringTest() throws Exception { 
        objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

       JaxbTask serverTask = new JaxbTask();
       List<I18NText> names = new ArrayList<I18NText>();
       serverTask.setPriority(2);
       serverTask.setNames(names);
       serverTask.setId(6l);
       JaxbI18NText text = new JaxbI18NText();
       text.setId(2l);
       text.setLanguage("nl-NL");
       text.setText("Doei!");
       names.add(text);
       JaxbTaskData taskData = new JaxbTaskData();
       serverTask.setJaxbTaskData(taskData);
       taskData.setActualOwnerId("me");
       taskData.setCreatedById("you");
       taskData.setCreatedOn(new Date());
       taskData.setDeploymentId("this");
       taskData.setDocumentContentId(0l);
       taskData.setDocumentType("this");
       taskData.setFaultContentId(1l);
       taskData.setFaultName("whoops");
       taskData.setFaultType("that");
       taskData.setOutputType("theirs");
       taskData.setSkipable(true);
       taskData.setWorkItemId(3l);
       taskData.setProcessInstanceId(3l);
       taskData.setOutputContentId(3l);
       taskData.setParentId(3l);
       taskData.setProcessSessionId(2);
       
       String jsonTaskStr = objectMapper.writeValueAsString(serverTask);
       logger.debug( jsonTaskStr );
       assertFalse( "String contains 'realClass' attribute", jsonTaskStr.contains("realClass"));
       Task clientTask = jsonProvider.deserialize(jsonTaskStr, Task.class);
       long id = clientTask.getId();
       assertEquals("task id", 6, id);
    }
}