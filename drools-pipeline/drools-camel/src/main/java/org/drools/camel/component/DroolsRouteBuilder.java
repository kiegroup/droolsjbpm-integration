/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.drools.camel.component;

import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;

public abstract class DroolsRouteBuilder extends RouteBuilder {
    @Override
    public RouteDefinition from(String uri) {
        return super.from(uri).routePolicy(new DroolsClassloaderPolicy());
    }
    
    @Override
    public RouteDefinition fromF(String uri, Object... args) {
        return super.fromF(uri, args).routePolicy(new DroolsClassloaderPolicy());
    }

    @Override
    public RouteDefinition from(Endpoint endpoint) {
        return super.from(endpoint).routePolicy(new DroolsClassloaderPolicy());
    }

    @Override
    public RouteDefinition from(String... uris) {
        return super.from(uris).routePolicy(new DroolsClassloaderPolicy());
    }
        
    @Override
    public RouteDefinition from(Endpoint... endpoints) {
        return super.from(endpoints).routePolicy(new DroolsClassloaderPolicy());
    }
}
