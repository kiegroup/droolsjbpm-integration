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

package org.kie.server.controller.websocket.client;

import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.impl.client.KieServicesClientProvider;
import org.kie.server.controller.websocket.WebSocketSessionManager;

public class WebSocketKieServerClientProvider implements KieServicesClientProvider {

    private WebSocketSessionManager manager = WebSocketSessionManager.getInstance();
    
    @Override
    public boolean supports(String url) {        
        return url.toLowerCase().startsWith("ws") || !manager.getByUrl(url).isEmpty();
    }

    @Override
    public KieServicesClient get(String url) {
        return new WebSocketKieServerClient(url);
    }

    @Override
    public Integer getPriority() {
        return 5;
    }

}
