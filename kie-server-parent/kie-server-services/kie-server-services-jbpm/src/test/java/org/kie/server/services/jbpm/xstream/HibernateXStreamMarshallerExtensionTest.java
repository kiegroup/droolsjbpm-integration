/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.jbpm.xstream;

import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.junit.Assert;
import org.junit.Test;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.mockito.Mockito;

import java.util.ArrayList;

public class HibernateXStreamMarshallerExtensionTest {

    @Test
    public void testMarshallDummyHibernatePersistenceBag() {
        SharedSessionContractImplementor session = Mockito.mock(SharedSessionContractImplementor.class);
        Mockito.when(session.isOpen()).thenReturn(true);
        Mockito.when(session.isConnected()).thenReturn(true);
        PersistentBag bag = new PersistentBag(session, new ArrayList<String>());
        String expectedOutput = "<list/>";
        Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.XSTREAM, getClass().getClassLoader());
        Assert.assertEquals(expectedOutput, marshaller.marshall(bag));
    }
}
