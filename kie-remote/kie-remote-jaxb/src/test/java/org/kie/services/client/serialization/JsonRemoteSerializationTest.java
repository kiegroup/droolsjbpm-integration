package org.kie.services.client.serialization;

import java.util.Arrays;
import java.util.List;

import org.jbpm.services.task.jaxb.ComparePair;
import org.junit.Test;
import org.kie.internal.runtime.conf.AuditMode;
import org.kie.internal.runtime.conf.NamedObjectModel;
import org.kie.internal.runtime.conf.ObjectModel;
import org.kie.internal.runtime.conf.PersistenceMode;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.services.client.AbstractRemoteSerializationTest;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentDescriptor;

public class JsonRemoteSerializationTest extends AbstractRemoteSerializationTest {

    public TestType getType() { 
        return TestType.JSON;
    }
    
    protected JsonSerializationProvider jsonProvider = new JsonSerializationProvider();
    
    public <T> T testRoundTrip(T in) throws Exception {
        String jsonStr = jsonProvider.serialize(in);
        logger.debug(jsonStr);
        jsonProvider.setDeserializeOutputClass(in.getClass());
        return (T) jsonProvider.deserialize(jsonStr);
    }

    public void addClassesToSerializationProvider(Class<?>... extraClass) {
        // no-op
    }

    @Test
    public void deploymentDescriptorTest() throws Exception { 
       JaxbDeploymentDescriptor depDesc = new JaxbDeploymentDescriptor();
      
       depDesc.setAuditMode(AuditMode.JMS);
       depDesc.setAuditPersistenceUnit("per-unit");
       String [] classes = { "class" };
       depDesc.setClasses(Arrays.asList(classes));
       NamedObjectModel [] nomArr = { new NamedObjectModel("resol", "name", "class", "param-1") };
       List<NamedObjectModel> noms = Arrays.asList(nomArr);
       depDesc.setConfiguration(noms);
       depDesc.setEnvironmentEntries(noms);
       ObjectModel [] omArr = { new ObjectModel("asdf", "id", "param-1") };
       List<ObjectModel> oms = Arrays.asList(omArr);
       depDesc.setEventListeners(oms);
       depDesc.setGlobals(noms);
       depDesc.setMarshallingStrategies(oms);
       depDesc.setPersistenceMode(PersistenceMode.JPA);
       depDesc.setPersistenceUnit("more-per-unit");
       String [] roles = { "chief", "chef", "cook" };
       depDesc.setRequiredRoles(Arrays.asList(roles));
       depDesc.setRuntimeStrategy(RuntimeStrategy.PER_PROCESS_INSTANCE);
       depDesc.setTaskEventListeners(oms);
       depDesc.setWorkItemHandlers(noms);
       
       JaxbDeploymentDescriptor copyDepDesc = testRoundTrip(depDesc);
       ComparePair.compareObjectsViaFields(depDesc, copyDepDesc);
    }
}