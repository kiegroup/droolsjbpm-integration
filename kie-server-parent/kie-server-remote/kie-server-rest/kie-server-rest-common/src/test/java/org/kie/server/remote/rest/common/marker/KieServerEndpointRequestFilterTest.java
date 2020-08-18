/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.remote.rest.common.marker;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.remote.rest.common.marker.KieServerEndpoint.EndpointType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class KieServerEndpointRequestFilterTest {

    @Mock
    private ResourceInfo resourceInfo;

    static class TestSimpleOperations {
        public void test1() {
            // nothing
        }
        public void test2() {
            // nothing
        }
    }

    static class TestAnnotationOperations {
        @KieServerEndpoint(categories = EndpointType.DEFAULT)
        public void test1() {
            // nothing
        }
        @KieServerEndpoint(categories = EndpointType.ALWAYS)
        public void test2() {
            // nothing
        }
        @KieServerEndpoint(categories = EndpointType.HISTORY)
        public void test3() {
            // nothing
        }
    }

    @KieServerEndpoint(categories = EndpointType.HISTORY)
    static class TestTypeAnnotationOperations {
        @KieServerEndpoint(categories = EndpointType.DEFAULT)
        public void test1() {
            // nothing
        }

        public void test2() {
            // nothing
        }
    }
    

    @InjectMocks
    private KieServerEndpointRequestFilter filter = new KieServerEndpointRequestFilter();

    @Test
    public void testGetAlwaysPass() throws Exception {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(resourceInfo.getResourceMethod()).thenReturn(TestSimpleOperations.class.getMethod("test1"));
        when(resourceInfo.getResourceClass()).thenReturn((Class) TestSimpleOperations.class);

        when(requestContext.getMethod()).thenReturn("GET");

        filter.filter(requestContext);

        verify(requestContext, times(0)).abortWith(anyObject());
    }

    @Test
    public void testOtherOperationNoPass() throws Exception {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(resourceInfo.getResourceMethod()).thenReturn(TestSimpleOperations.class.getMethod("test1"));
        when(resourceInfo.getResourceClass()).thenReturn((Class) TestSimpleOperations.class);

        when(requestContext.getMethod()).thenReturn("POST");

        filter.filter(requestContext);

        verify(requestContext, times(1)).abortWith(anyObject());
    }

    @Test
    public void testOtherNoPassAnnotationPass() throws Exception {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(resourceInfo.getResourceMethod()).thenReturn(TestAnnotationOperations.class.getMethod("test1"));
        when(resourceInfo.getResourceClass()).thenReturn((Class) TestAnnotationOperations.class);

        when(requestContext.getMethod()).thenReturn("POST");

        filter.filter(requestContext);

        verify(requestContext, times(1)).abortWith(anyObject());
    }

    @Test
    public void testOtherAlwaysPassAnnotationPass() throws Exception {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(resourceInfo.getResourceMethod()).thenReturn(TestAnnotationOperations.class.getMethod("test2"));
        when(resourceInfo.getResourceClass()).thenReturn((Class) TestAnnotationOperations.class);

        when(requestContext.getMethod()).thenReturn("POST");

        filter.filter(requestContext);

        verify(requestContext, times(0)).abortWith(anyObject());
    }

    @Test
    public void testOtherOverrideBadPassAnnotationPass() throws Exception {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(resourceInfo.getResourceMethod()).thenReturn(TestTypeAnnotationOperations.class.getMethod("test1"));
        when(resourceInfo.getResourceClass()).thenReturn((Class) TestTypeAnnotationOperations.class);

        when(requestContext.getMethod()).thenReturn("POST");

        filter.filter(requestContext);

        verify(requestContext, times(1)).abortWith(anyObject());
    }

    @Test
    public void testOtherTypePassAnnotationPass() throws Exception {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(resourceInfo.getResourceMethod()).thenReturn(TestAnnotationOperations.class.getMethod("test2"));
        when(resourceInfo.getResourceClass()).thenReturn((Class) TestAnnotationOperations.class);

        when(requestContext.getMethod()).thenReturn("POST");

        filter.filter(requestContext);

        verify(requestContext, times(0)).abortWith(anyObject());
    }
}
