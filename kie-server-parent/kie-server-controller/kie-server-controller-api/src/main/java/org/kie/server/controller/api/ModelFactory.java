/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

import java.net.MalformedURLException;
import java.net.URL;

import org.kie.server.controller.api.model.runtime.ServerInstanceKey;

public class ModelFactory {


    public static ServerInstanceKey newServerInstanceKey(String serverTemplateId, String url) {
        try {
            URL serverUrl = new URL(url);
            String serverInstanceId = null;
            if (serverUrl.getPort() == -1) {
                serverInstanceId = serverTemplateId + "@" + serverUrl.getHost();
            } else {
                serverInstanceId = serverTemplateId + "@" + serverUrl.getHost() + ":" + serverUrl.getPort();
            }
            return new ServerInstanceKey(serverTemplateId, serverInstanceId, serverInstanceId, url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid server url " + url);
        }

    }
}
