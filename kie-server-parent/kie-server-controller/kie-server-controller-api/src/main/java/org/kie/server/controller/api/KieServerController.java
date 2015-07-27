/*
 * Copyright 2015 JBoss Inc
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

package org.kie.server.controller.api;

import org.kie.server.api.model.KieServerInfo;
import org.kie.server.controller.api.model.KieServerSetup;

/**
 * KieServer facing controller that allows KieServers to inform about its availability.
 * Upon connection it will receive KieServerSetup instance known to the controller. If none is available an empty
 * one will be provided.
 */
public interface KieServerController {

    /**
     * Entry point for for KieServer to connect(and register if done for the first time). At the same time, when given KieServerInstance
     * has been already added a KieServerSetup with data will be returned. Otherwise empty (or default) KieServerSetup will be provided.
     * @param serverInfo representation of minimal set of information about KieServer
     * @return KieServer configuration
     */
    KieServerSetup connect(KieServerInfo serverInfo);

    /**
     * Notifies controller that server is going down.
     * @param serverInfo representation of minimal set of information about KieServer
     */
    void disconnect(KieServerInfo serverInfo);
}
