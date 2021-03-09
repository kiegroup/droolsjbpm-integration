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

package org.kie.server.remote.rest.swagger;

import static java.util.Collections.emptyListIterator;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.kie.server.remote.rest.swagger.ReadOnlySwaggerDecoration.SWAGGER_EXTENSION_READ_ONLY;

import javax.ws.rs.GET;
import javax.ws.rs.POST;

import org.junit.Assert;
import org.junit.Test;
import org.kie.server.remote.rest.common.marker.KieServerEndpoint;
import org.kie.server.remote.rest.common.marker.KieServerEndpoint.EndpointType;

import io.swagger.models.Operation;

public class ReadOnlySwaggerDecorationTest {

    
    static class TestSimpleOperations {
        @GET
        public void test1() {
            // nothing
        }
        @POST
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
    

    @Test
    public void testGetAlwaysPass() throws Exception {
        ReadOnlySwaggerDecoration decoration = new ReadOnlySwaggerDecoration();
        Operation operation = new Operation();
        decoration.decorateOperation(operation, TestSimpleOperations.class.getMethod("test1"), emptyListIterator());

        Assert.assertThat(operation.getVendorExtensions().keySet(), hasItems(SWAGGER_EXTENSION_READ_ONLY));
    }

    @Test
    public void testOtherOperationNoPass() throws Exception {
        ReadOnlySwaggerDecoration decoration = new ReadOnlySwaggerDecoration();
        Operation operation = new Operation();
        decoration.decorateOperation(operation, TestSimpleOperations.class.getMethod("test2"), emptyListIterator());

        Assert.assertThat(operation.getVendorExtensions().keySet(), not(hasItems(SWAGGER_EXTENSION_READ_ONLY)));
    }

    @Test
    public void testOtherNoPassAnnotationPass() throws Exception {
        ReadOnlySwaggerDecoration decoration = new ReadOnlySwaggerDecoration();
        Operation operation = new Operation();
        decoration.decorateOperation(operation, TestAnnotationOperations.class.getMethod("test1"), emptyListIterator());

        Assert.assertThat(operation.getVendorExtensions().keySet(), not(hasItems(SWAGGER_EXTENSION_READ_ONLY)));

    }

    @Test
    public void testOtherAlwaysPassAnnotationPass() throws Exception {
        ReadOnlySwaggerDecoration decoration = new ReadOnlySwaggerDecoration();
        Operation operation = new Operation();
        decoration.decorateOperation(operation, TestAnnotationOperations.class.getMethod("test2"), emptyListIterator());

        Assert.assertThat(operation.getVendorExtensions().keySet(), hasItems(SWAGGER_EXTENSION_READ_ONLY));
    }

    @Test
    public void testOtherOverrideBadPassAnnotationPass() throws Exception {
        ReadOnlySwaggerDecoration decoration = new ReadOnlySwaggerDecoration();
        Operation operation = new Operation();
        decoration.decorateOperation(operation, TestTypeAnnotationOperations.class.getMethod("test1"), emptyListIterator());
        Assert.assertThat(operation.getVendorExtensions().keySet(), not(hasItems(SWAGGER_EXTENSION_READ_ONLY)));

    }

    @Test
    public void testOtherTypePassAnnotationPass() throws Exception {
        ReadOnlySwaggerDecoration decoration = new ReadOnlySwaggerDecoration();
        Operation operation = new Operation();
        decoration.decorateOperation(operation, TestTypeAnnotationOperations.class.getMethod("test2"), emptyListIterator());
        Assert.assertThat(operation.getVendorExtensions().keySet(), hasItems(SWAGGER_EXTENSION_READ_ONLY));
    }
}
