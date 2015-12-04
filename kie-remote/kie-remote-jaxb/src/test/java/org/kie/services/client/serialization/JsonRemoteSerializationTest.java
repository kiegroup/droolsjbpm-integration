/*
 * Copyright 2015 JBoss Inc
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

package org.kie.services.client.serialization;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.kie.internal.runtime.conf.AuditMode;
import org.kie.internal.runtime.conf.NamedObjectModel;
import org.kie.internal.runtime.conf.ObjectModel;
import org.kie.internal.runtime.conf.PersistenceMode;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentDescriptor;
import org.kie.test.util.compare.ComparePair;

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
       depDesc.setRemoteableClasses(Arrays.asList(classes));
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