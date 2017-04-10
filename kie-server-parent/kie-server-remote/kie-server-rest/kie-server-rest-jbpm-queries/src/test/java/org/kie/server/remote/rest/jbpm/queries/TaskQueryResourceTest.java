package org.kie.server.remote.rest.jbpm.queries;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.instance.TaskInstanceList;
import org.kie.server.jbpm.queries.api.model.definition.TaskQueryFilterSpec;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.queries.TaskQueryServiceBase;
import org.skyscreamer.jsonassert.JSONAssert;
import org.xmlunit.matchers.CompareMatcher;

public class TaskQueryResourceTest {

	@Test
	public void testGetHumanTasksWithFiltersXML() throws Exception {
		
		TaskQueryServiceBase tqsbMock = mock(TaskQueryServiceBase.class);
		KieServerRegistry contextMock = mock(KieServerRegistry.class);
		
		//Registry mock needs to return extra classes registered by the extension.
		Set<Class<?>> extraClasses = new HashSet<>();
		extraClasses.add(TaskQueryFilterSpec.class);
		when(contextMock.getExtraClasses()).thenReturn(extraClasses);
		
		when(tqsbMock.getHumanTasksWithFilters(any(), any(), any(), eq("JAXB"))).thenReturn(getTaskInstanceList());
		
		TaskQueryResource tqr = new TaskQueryResource(tqsbMock, contextMock);
		
		MultivaluedMap<String, String> headers = new MultivaluedMapImpl<>();
		headers.put(KieServerConstants.KIE_CONTENT_TYPE_HEADER, Collections.singletonList("JAXB"));
		headers.put(HttpHeaders.CONTENT_TYPE, Collections.singletonList(MediaType.APPLICATION_XML));
		headers.put(HttpHeaders.ACCEPT, Collections.singletonList(MediaType.APPLICATION_JSON));
		
		HttpHeaders httpHeaders = new ResteasyHttpHeaders(headers);
		
		Response response = tqr.getHumanTasksWithFilters(httpHeaders, 0, 10, getPayload());
		String responseEntity = (String) response.getEntity();
		
		String expectedResponseEntity = "<task-instance-list/>";
		
		assertThat(responseEntity, CompareMatcher.isIdenticalTo(expectedResponseEntity).ignoreWhitespace());
	}
	
	@Test
	public void testGetHumanTasksWithFilterJSON() throws Exception {
		
		TaskQueryServiceBase tqsbMock = mock(TaskQueryServiceBase.class);
		KieServerRegistry contextMock = mock(KieServerRegistry.class);
		
		//Registry mock needs to return extra classes registered by the extension.
		Set<Class<?>> extraClasses = new HashSet<>();
		extraClasses.add(TaskQueryFilterSpec.class);
		when(contextMock.getExtraClasses()).thenReturn(extraClasses);
		
		when(tqsbMock.getHumanTasksWithFilters(any(), any(), any(), eq("JSON"))).thenReturn(getTaskInstanceList());
		
		TaskQueryResource tqr = new TaskQueryResource(tqsbMock, contextMock);
		
		MultivaluedMap<String, String> headers = new MultivaluedMapImpl<>();
		headers.put(KieServerConstants.KIE_CONTENT_TYPE_HEADER, Collections.singletonList("JSON"));
		headers.put(HttpHeaders.CONTENT_TYPE, Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.put(HttpHeaders.ACCEPT, Collections.singletonList(MediaType.APPLICATION_JSON));
		
		HttpHeaders httpHeaders = new ResteasyHttpHeaders(headers);
		
		Response response = tqr.getHumanTasksWithFilters(httpHeaders, 0, 10, getPayload());
		String responseEntity = (String) response.getEntity();
		
		String expectedResponseEntity = new StringBuilder().append("{").append("\"task-instance\" : null").append("}").toString();
		
		JSONAssert.assertEquals(expectedResponseEntity, responseEntity, false);
	}
	
	private static String getPayload() {
		StringBuilder payloadBuilder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"); 
		payloadBuilder.append("<task-query-filter-spec>");
		payloadBuilder.append("<order-asc>false</order-asc>");
		payloadBuilder.append("<query-params>");
		payloadBuilder.append("<cond-column>DEPLOYMENTID</cond-column>");
		payloadBuilder.append("<cond-operator>EQUALS_TO</cond-operator>");
		payloadBuilder.append("<cond-values xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">simple-project</cond-values>");
		payloadBuilder.append("</query-params>");
		payloadBuilder.append("</task-query-filter-spec>");
		return payloadBuilder.toString();
	}
	
	private static TaskInstanceList getTaskInstanceList() {
		return new TaskInstanceList();
	}
}
