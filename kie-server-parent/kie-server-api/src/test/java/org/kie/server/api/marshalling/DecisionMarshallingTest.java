/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.api.marshalling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.runtime.KieSession;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.internal.utils.KieHelper;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.dmn.DMNContextKS;
import org.kie.server.api.model.dmn.DMNResultKS;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class DecisionMarshallingTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(
                new Object[][]{
                        {MarshallingFormat.JAXB},
                        {MarshallingFormat.JSON},
                        {MarshallingFormat.XSTREAM}
                }
        ));

        return parameterData;
    }

    @Parameterized.Parameter(0)
    public MarshallingFormat marshallingFormat;

    private Marshaller marshaller;

    @Before
    public void setUp() {
        marshaller = MarshallerFactory.getMarshaller(marshallingFormat, Thread.currentThread().getContextClassLoader());
    }

    @Test
    public void testMarshalling() {
        KieSession kieSession = new KieHelper().addFromClassPath("/FunctionDefinition.dmn").build().newKieSession();
        DMNRuntime dmnRuntime = kieSession.getKieRuntime(DMNRuntime.class);
        DMNModel model = dmnRuntime.getModels().get(0);

        DMNContext realCtx = dmnRuntime.newContext();
        realCtx.set("a", 10);
        realCtx.set("b", 5);
        DMNContextKS dmnClientRequest = new DMNContextKS(realCtx.getAll());

        DMNContextKS mu_dmnClientRequest = marshallUnmarshall(dmnClientRequest);
        assertEquals(dmnClientRequest.getNamespace(), mu_dmnClientRequest.getNamespace());
        assertEquals(dmnClientRequest.getModelName(), mu_dmnClientRequest.getModelName());
        assertThat(dmnClientRequest.getDecisionNames(), is(mu_dmnClientRequest.getDecisionNames()));
        assertEquals(dmnClientRequest.getDmnContext().size(), mu_dmnClientRequest.getDmnContext().size());
        assertEquals(dmnClientRequest.getDmnContext().keySet(), mu_dmnClientRequest.getDmnContext().keySet());

        DMNResult evaluateAll = dmnRuntime.evaluateAll(model, realCtx);
        ServiceResponse<DMNResultKS> dmnClientResponse = new ServiceResponse<DMNResultKS>(
                ServiceResponse.ResponseType.SUCCESS,
                "Test case",
                new DMNResultKS(model.getNamespace(), model.getName(), dmnClientRequest.getDecisionNames(), evaluateAll));
        ServiceResponse<DMNResultKS> mu_dmnClientResponse = marshallUnmarshall(dmnClientResponse);
        assertEquals(dmnClientResponse.getResult().getNamespace(), mu_dmnClientResponse.getResult().getNamespace());
        assertEquals(dmnClientResponse.getResult().getModelName(), mu_dmnClientResponse.getResult().getModelName());
        assertThat(dmnClientResponse.getResult().getDecisionNames(), is(mu_dmnClientResponse.getResult().getDecisionNames()));
        assertEquals(dmnClientResponse.getResult().getDmnContext().size(), mu_dmnClientResponse.getResult().getDmnContext().size());
        assertEquals(dmnClientResponse.getResult().getDmnContext().keySet(), mu_dmnClientResponse.getResult().getDmnContext().keySet());
    }

    @SuppressWarnings("unchecked")
    private <V> V marshallUnmarshall(V input) {
        try {
            String marshall = marshaller.marshall(input);
            System.out.println(marshall);
            V unmarshall = (V) marshaller.unmarshall(marshall, input.getClass());
            return unmarshall;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
