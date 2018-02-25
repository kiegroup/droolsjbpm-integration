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

package org.kie.server.integrationtests.dmn;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.dmn.DMNInputDataInfo;
import org.kie.server.api.model.dmn.DMNItemDefinitionInfo;
import org.kie.server.api.model.dmn.DMNModelInfo;
import org.kie.server.api.model.dmn.DMNModelInfoList;
import org.kie.server.api.model.dmn.DMNQNameInfo;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class DMNGetModelsIntegrationTest
        extends DMNKieServerBaseIntegrationTest {

    private static final ReleaseId kjar1 = new ReleaseId("org.kie.server.testing", "get-models", "1.0.0.Final");

    private static final String CONTAINER_1_ID = "get-models";

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/get-models").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(kjar1);
        createContainer(CONTAINER_1_ID, kjar1);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses)
            throws Exception {

        // no extra classes.
    }

    @Test
    public void test_getModels() {
        ServiceResponse<DMNModelInfoList> modelInfoListResponse = dmnClient.getModels(CONTAINER_1_ID);

        assertThat(modelInfoListResponse.getType(), is(ResponseType.SUCCESS));

        List<DMNModelInfo> models = modelInfoListResponse.getResult().getModels();

        assertThat(models, hasSize(1));

        DMNModelInfo modelInfo = models.get(0);
        
        assertThat(modelInfo.getName(), is("a number a string a list of numbers"));
        
        Collection<DMNInputDataInfo> inputs = modelInfo.getInputs();
        assertThat(inputs, hasSize(4));

        assertCollectionOfInputDataForNameHasTypeRef(inputs, "a number", "feel", "number");
        assertCollectionOfInputDataForNameHasTypeRef(inputs, "a string", "feel", "string");
        assertCollectionOfInputDataForNameHasTypeRef(inputs, "a list of numbers", XMLConstants.DEFAULT_NS_PREFIX, "tList");
        assertCollectionOfInputDataForNameHasTypeRef(inputs, "a Person", XMLConstants.DEFAULT_NS_PREFIX, "tPerson");

        Collection<DMNItemDefinitionInfo> itemDefs = modelInfo.getItemDefinitions();
        assertThat(itemDefs, hasSize(2));
        DMNItemDefinitionInfo tList = itemDefs.stream().filter(id -> id.getName().equals("tList")).findFirst().get();
        assertThat(tList.getTypeRef().getLocalPart(), is("number"));
        assertThat(tList.getTypeRef().getPrefix(), is("feel"));
        assertThat(tList.getIsCollection(), is(true));

        DMNItemDefinitionInfo tPerson = itemDefs.stream().filter(id -> id.getName().equals("tPerson")).findFirst().get();
        assertThat(tPerson.getTypeRef(), nullValue());
        assertThat(tPerson.getIsCollection(), is(false));
        assertThat(tPerson.getItemComponent(), hasSize(3));
        assertThat(tPerson.getItemComponent().get(0).getName(), is("full name"));
        assertThat(tPerson.getItemComponent().get(0).getTypeRef().getLocalPart(), is("string"));
        assertThat(tPerson.getItemComponent().get(0).getTypeRef().getPrefix(), is("feel"));
        assertThat(tPerson.getItemComponent().get(1).getName(), is("age"));
        assertThat(tPerson.getItemComponent().get(1).getTypeRef().getLocalPart(), is("number"));
        assertThat(tPerson.getItemComponent().get(1).getTypeRef().getPrefix(), is("feel"));
        assertThat(tPerson.getItemComponent().get(2).getName(), is("favorite numbers"));
        assertThat(tPerson.getItemComponent().get(2).getTypeRef().getLocalPart(), is("tList"));
        assertThat(tPerson.getItemComponent().get(2).getTypeRef().getPrefix(), is(XMLConstants.DEFAULT_NS_PREFIX));
    }

    private void assertCollectionOfInputDataForNameHasTypeRef(Collection<DMNInputDataInfo> inputs, String name, String typeRefPrefix, String typeRefLocalPart) {
        DMNInputDataInfo idANumber = inputs.stream().filter(id -> id.getName().equals(name)).findFirst().get();
        DMNQNameInfo typeRef = idANumber.getTypeRef();
        assertThat(typeRef.getLocalPart(), is(typeRefLocalPart));
        assertThat(typeRef.getPrefix(), is(typeRefPrefix));
    }

}
