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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Test;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.util.QueryFilterSpecBuilder;
import org.kie.server.services.api.KieServerRegistry;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.skyscreamer.jsonassert.JSONAssert;
import org.xmlunit.matchers.CompareMatcher;

public class MarshallerHelperTest {

	@Test
	public void testMarshallWithoutContainer() {
		KieServerRegistry kieServerRegistryMock = Mockito.mock(KieServerRegistry.class);

		MarshallerHelper helper = new MarshallerHelper(kieServerRegistryMock);

		QueryFilterSpec queryFilterSpec = new QueryFilterSpecBuilder().get();

		String marshalledQFS = helper.marshal(MarshallingFormat.JAXB.toString(), queryFilterSpec);

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

		String expectedMarshalledTEC = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<test-extra-class>"
				+ "<bla>hallo</bla>" + "</test-extra-class>";

		assertThat(marshalledQFS, CompareMatcher.isIdenticalTo(expectedMarshalledTEC).ignoreWhitespace());
	}
	
	@Test
	public void testJsonMarshallWithoutWithEmptyRegistry() throws Exception {
		
		KieServerRegistry kieServerRegistryMock = Mockito.mock(KieServerRegistry.class);
		Mockito.when(kieServerRegistryMock.getExtraClasses()).thenReturn(new HashSet<Class<?>>());
		
		MarshallerHelper helper = new MarshallerHelper(kieServerRegistryMock);

		QueryFilterSpec queryFilterSpec = new QueryFilterSpecBuilder().get();
		
		String marshalledQFS = helper.marshal(MarshallingFormat.JSON.toString(), queryFilterSpec);
		
		System.out.println(marshalledQFS);
		
		String expectedMarshalledTEC = "{\"order-by\" : null, \"order-asc\" : false, \"query-params\" : null, \"result-column-mapping\" : null}";

		//assertThat(marshalledQFS, equalToIgnoringWhiteSpace(expectedMarshalledTEC));
		JSONAssert.assertEquals(expectedMarshalledTEC, marshalledQFS, false);
	}
	
	@Test
	public void testJsonMarshallWithNullRegistry() throws Exception {
		
		
		MarshallerHelper helper = new MarshallerHelper(null);

		QueryFilterSpec queryFilterSpec = new QueryFilterSpecBuilder().get();
		
		String marshalledQFS = helper.marshal(MarshallingFormat.JSON.toString(), queryFilterSpec);
		
		System.out.println(marshalledQFS);
		
		String expectedMarshalledTEC = "{\"order-by\" : null, \"order-asc\" : false, \"query-params\" : null, \"result-column-mapping\" : null}";

		//assertThat(marshalledQFS, equalToIgnoringWhiteSpace(expectedMarshalledTEC));
		JSONAssert.assertEquals(expectedMarshalledTEC, marshalledQFS, false);
	}
	

	/**
	 * Tests that MarshallerHelper can also be used when passing in a <code>null</code> KieServerRegistry.
	 */
	@Test
	public void testMarshallWithNullRegistry() {
		MarshallerHelper helper = new MarshallerHelper(null);

		QueryFilterSpec queryFilterSpec = new QueryFilterSpecBuilder().get();

		String marshalledQFS = helper.marshal(MarshallingFormat.JAXB.toString(), queryFilterSpec);

		String expectedMarshalledQFS = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<query-filter-spec>"
				+ "<order-asc>false</order-asc>" + "</query-filter-spec>";

		assertThat(marshalledQFS, CompareMatcher.isIdenticalTo(expectedMarshalledQFS).ignoreWhitespace());
	}

	@Test
	public void testUnmarshallWithoutContainer() {
		KieServerRegistry kieServerRegistryMock = Mockito.mock(KieServerRegistry.class);

		MarshallerHelper helper = new MarshallerHelper(kieServerRegistryMock);

		QueryFilterSpec expectedQueryFilterSpec = new QueryFilterSpecBuilder().get();

		String marshalledQFS = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<query-filter-spec>"
				+ "<order-asc>false</order-asc>" + "</query-filter-spec>";

		QueryFilterSpec unmarshalledQFS = helper.unmarshal(marshalledQFS, MarshallingFormat.JAXB.toString(), QueryFilterSpec.class);

		// QueryFilterSpec does not implement equals method, so using Mockito ReflectionEquals.
		assertThat(expectedQueryFilterSpec, new ReflectionEquals(unmarshalledQFS));
	}

	@Test
	public void testUnmarshallWithoutContainerWithExtraClasses() {
		KieServerRegistry kieServerRegistryMock = Mockito.mock(KieServerRegistry.class);

		Set<Class<?>> extraClasses = new HashSet<>();
		extraClasses.add(TestExtraClass.class);

		Mockito.when(kieServerRegistryMock.getExtraClasses()).thenReturn(extraClasses);

		MarshallerHelper helper = new MarshallerHelper(kieServerRegistryMock);

		TestExtraClass expectedExtraClass = new TestExtraClass();
		expectedExtraClass.setBla("hallo");

		String marshalledTEC = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<test-extra-class>" + "<bla>hallo</bla>"
				+ "</test-extra-class>";

		TestExtraClass unmarshalledTEC = helper.unmarshal(marshalledTEC, MarshallingFormat.JAXB.toString(), TestExtraClass.class);

		assertEquals(expectedExtraClass, unmarshalledTEC);
	}

	/**
	 * Tests that MarshallerHelper can also be used when passing in a <code>null</code> KieServerRegistry.
	 */
	@Test
	public void testUnmarshallWithoutNullRegistry() {
		MarshallerHelper helper = new MarshallerHelper(null);

		QueryFilterSpec expectedQueryFilterSpec = new QueryFilterSpecBuilder().get();

		String marshalledQFS = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<query-filter-spec>"
				+ "<order-asc>false</order-asc>" + "</query-filter-spec>";

		QueryFilterSpec unmarshalledQFS = helper.unmarshal(marshalledQFS, MarshallingFormat.JAXB.toString(), QueryFilterSpec.class);

		// QueryFilterSpec does not implement equals method, so using Mockito ReflectionEquals.
		assertThat(expectedQueryFilterSpec, new ReflectionEquals(unmarshalledQFS));
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

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj == this) {
				return true;
			}
			if (obj.getClass() != getClass()) {
				return false;
			}
			TestExtraClass rhs = (TestExtraClass) obj;
			return new EqualsBuilder().append(bla, rhs.bla).isEquals();
		}

	}

}
