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

package org.kie.server.services.impl.marshal;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.Test;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.util.QueryFilterSpecBuilder;
import org.kie.server.services.api.KieServerRegistry;
import org.mockito.Mockito;
import org.xmlunit.matchers.CompareMatcher;

public class MarshallerHelperTest {

	@Test
	public void testMarshallWithoutContainer() {
		KieServerRegistry kieServerRegistryMock = Mockito.mock(KieServerRegistry.class);

		MarshallerHelper helper = new MarshallerHelper(kieServerRegistryMock);

		QueryFilterSpec queryFilterSpec = new QueryFilterSpecBuilder().get();

		String marshalledQFS = helper.marshal(MarshallingFormat.JAXB.toString(), queryFilterSpec);
		System.out.println(marshalledQFS);

		String expectedMarshalledQFS = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<query-filter-spec>"
				+ "<order-asc>false</order-asc>" + "</query-filter-spec>";

		assertThat(marshalledQFS, CompareMatcher.isIdenticalTo(expectedMarshalledQFS).ignoreWhitespace());

	}

	@Test
	public void testMarshallWithoutContainerWithExtraClasses() {
		KieServerRegistry kieServerRegistryMock = Mockito.mock(KieServerRegistry.class);

		Set<Class<?>> extraClasses = new HashSet<>();
		extraClasses.add(TestExtraClass.class);

		Mockito.when(kieServerRegistryMock.getExtraClasses()).thenReturn(extraClasses);

		MarshallerHelper helper = new MarshallerHelper(kieServerRegistryMock);

		TestExtraClass extraClass = new TestExtraClass();
		extraClass.setBla("hallo");

		String marshalledQFS = helper.marshal(MarshallingFormat.JAXB.toString(), extraClass);
		System.out.println(marshalledQFS);

		String expectedMarshalledTEC = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<test-extra-class>"
				+ "<bla>hallo</bla>" + "</test-extra-class>";

		assertThat(marshalledQFS, CompareMatcher.isIdenticalTo(expectedMarshalledTEC).ignoreWhitespace());
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "test-extra-class")
	public static class TestExtraClass {
		@XmlElement(name = "bla")
		private String bla;

		public String getBla() {
			return bla;
		}

		public void setBla(String bla) {
			this.bla = bla;
		}
	}

}
