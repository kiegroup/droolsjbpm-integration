package org.kie.services.client.message.serialization.impl.jaxb;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.kie.services.client.api.ApiRequestFactoryProvider;
import org.kie.services.client.api.MessageHolder;
import org.kie.services.client.api.same.SameApiRequestHandler;
import org.kie.services.client.api.same.SameApiRequestTest;
import org.kie.services.client.message.OperationMessage;
import org.kie.services.client.message.ServiceMessage;
import org.kie.services.client.message.serialization.MessageSerializationProvider.Type;
import org.kie.services.client.message.serialization.impl.JaxbSerializationProvider;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleJaxbTest extends Assert {

    Logger logger = LoggerFactory.getLogger(SimpleJaxbTest.class);

    @Test
    public void shouldBeAbleToMarshallSimpleMessage() throws Exception {
        SameApiRequestHandler requestFactory = ApiRequestFactoryProvider.createNewSameApiInstance();
        requestFactory.setSerialization(Type.JAXB);

        RuntimeEngine remoteRuntimeEngine = requestFactory.getRemoteRuntimeEngine("domain");
        TaskService taskServiceRequest = remoteRuntimeEngine.getTaskService();

        taskServiceRequest.activate(1, "Danno");
        taskServiceRequest.claimNextAvailable("Steve", "en-UK");

        ServiceMessage serviceMsg = ((MessageHolder) taskServiceRequest).getRequest();
        JaxbServiceMessage jaxbMsg = new JaxbServiceMessage(serviceMsg);

        // marshall
        String xmlStr = JaxbSerializationProvider.convertJaxbObjectToString(jaxbMsg);
        assertTrue("String is empty or too short", xmlStr != null && xmlStr.length() > 40);
        logger.info(xmlStr);
    }

    @Test
    public void shouldBeAbleToMarshallMoreSimpleMessages() throws Exception {
        SameApiRequestHandler requestFactory = ApiRequestFactoryProvider.createNewSameApiInstance();
        requestFactory.setSerialization(Type.JAXB);

        RuntimeEngine remoteRuntimeEngine = requestFactory.getRemoteRuntimeEngine("domain");
        TaskService taskServiceRequest = remoteRuntimeEngine.getTaskService();

        taskServiceRequest.activate(1, "Danno");
        taskServiceRequest.claimNextAvailable("Steve", "en-UK");

        ServiceMessage serviceMsg = ((MessageHolder) taskServiceRequest).getRequest();

        for (OperationMessage oper : serviceMsg.getOperations()) {
            oper.setResult("RESULT!");
        }
        JaxbServiceMessage jaxbMsg = new JaxbServiceMessage(serviceMsg);

        // marshall
        String xmlStr = JaxbSerializationProvider.convertJaxbObjectToString(jaxbMsg);
        assertTrue("String is empty or too short", xmlStr != null && xmlStr.length() > 40);
        logger.info(xmlStr);
    }

    @Test
    public void shouldBeAbleToMarshall() throws Exception {
        // Factory
        SameApiRequestHandler requestFactory = ApiRequestFactoryProvider.createNewSameApiInstance();
        requestFactory.setSerialization(Type.JAXB);

        RuntimeEngine remoteRuntimeEngine = requestFactory.getRemoteRuntimeEngine("domain");
        KieSession sessionRequest = remoteRuntimeEngine.getKieSession();

        // Operation
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("jersey", new Long(5));
        sessionRequest.startProcess("batter-up", params);

        // Convert to message
        ServiceMessage serviceMsg = ((MessageHolder) sessionRequest).getRequest();
        JaxbServiceMessage jaxbMsg = new JaxbServiceMessage(serviceMsg);

        // marshall
        String xmlStr = JaxbSerializationProvider.convertJaxbObjectToString(jaxbMsg);
        assertTrue("String is empty or too short", xmlStr != null && xmlStr.length() > 40);
        logger.info(xmlStr);
    }

    @Test
    public void shouldBeAbleToMarshallMultipleOp() throws Exception {
        ServiceMessage serviceMsg = SameApiRequestTest.createMultipleOpRequest();
        JaxbServiceMessage jaxbMsg = new JaxbServiceMessage(serviceMsg);

        // marshall
        String xmlStr = JaxbSerializationProvider.convertJaxbObjectToString(jaxbMsg);
        assertTrue("String is empty or too short", xmlStr != null && xmlStr.length() > 40);
        logger.info(xmlStr);
    }

    @Test
    public void roundTripShouldWork() throws Exception {
        String thirdOperSecondArg = "Clavier";
        ServiceMessage request = SameApiRequestTest.createMultipleOpRequest();

        String xmlStr = JaxbSerializationProvider.convertJaxbObjectToString(new JaxbServiceMessage(request));
        JaxbServiceMessage jaxbMsg = (JaxbServiceMessage) JaxbSerializationProvider.convertStringToJaxbObject(xmlStr);
        ServiceMessage roundTripRequest = JaxbSerializationProvider.convertJaxbRequesMessageToServiceMessage(jaxbMsg);

        roundTripRequest.getDomainName();
        // TODO: test equals code
    }

    @Test
    public void integrationRequestShouldBeNormal() throws Exception {
        SameApiRequestHandler requestFactory = ApiRequestFactoryProvider.createNewSameApiInstance();

        // create service request
        RuntimeEngine remoteRuntimeEngine = requestFactory.getRemoteRuntimeEngine("test");
        KieSession serviceRequest = remoteRuntimeEngine.getKieSession();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("user-id", "Lin Dze");
        serviceRequest.startProcess("org.jbpm.scripttask", params);

        String msgXmlString = ((MessageHolder) serviceRequest).getMessageXmlString();
        System.out.println(msgXmlString);
    }
}
