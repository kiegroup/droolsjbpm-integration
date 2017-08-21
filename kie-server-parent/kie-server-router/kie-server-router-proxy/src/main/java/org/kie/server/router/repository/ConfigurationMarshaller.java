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

package org.kie.server.router.repository;

import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.kie.server.router.Configuration;
import org.kie.server.router.ContainerInfo;

public class ConfigurationMarshaller {

    public String marshall(Configuration configuration) throws Exception {
        Map<String, List<String>> perContainer = configuration.getHostsPerContainer();
        Map<String, List<String>> perServer = configuration.getHostsPerServer();
        Map<String, List<ContainerInfo>> containerInfo = configuration.getContainerInfosPerContainer();
        
        JSONArray servers = new JSONArray();
        JSONArray containers = new JSONArray();
        JSONArray infos = new JSONArray();
        JSONObject config = new JSONObject();            
        
        for (Entry<String, List<String>> entry : perContainer.entrySet()) {
            JSONArray array = new JSONArray();
            entry.getValue().forEach(url -> array.put(url));
            
            JSONObject container = new JSONObject();
            container.put(entry.getKey(), array);
            
            containers.put(container);
        }
        
        for (Entry<String, List<String>> entry : perServer.entrySet()) {
            JSONArray array = new JSONArray();
            entry.getValue().forEach(url -> array.put(url));
            
            JSONObject server = new JSONObject();
            server.put(entry.getKey(), array);
            
            servers.put(server);
        }
        Set<String> processed = new HashSet<>();
        for (Entry<String, List<ContainerInfo>> entry : containerInfo.entrySet()) {
            if (processed.contains(entry.getKey())) {
                continue;
            }
            entry.getValue().forEach(ci -> {
                JSONObject jsonCI = new JSONObject();
                processed.add(ci.getAlias());
                processed.add(ci.getContainerId());
                try {
                    jsonCI.put("alias", ci.getAlias());
                    jsonCI.put("containerId", ci.getContainerId());
                    jsonCI.put("releaseId", ci.getReleaseId());
                    infos.put(jsonCI);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            });
        }
        
        config.put("containers", containers);
        config.put("servers", servers);
        config.put("containerInfo", infos);
        
        return config.toString(2);
    }
    
    public Configuration unmarshall(Reader reader) throws Exception {
        Configuration configuration = new Configuration();
        JSONTokener tokener = new JSONTokener(reader);                
        JSONObject config = new JSONObject(tokener);
        
        JSONArray containers = config.getJSONArray("containers");
        for (int i = 0; i < containers.length(); i++) {
            JSONObject container = (JSONObject)containers.get(i);
            
            String[] names = JSONObject.getNames(container);
            
            for (String name : names) {
                JSONArray urls = (JSONArray) container.get(name);
                if (urls.length() > 0) {
                    for (int j = 0; j < urls.length(); j++) {
                        String url = urls.get(j).toString();

                        configuration.addContainerHost(name, url);
                    }
                } else {
                    configuration.addEmptyContainerHost(name);
                }
            }
        }
        
        JSONArray servers = config.getJSONArray("servers");
        for (int i = 0; i < servers.length(); i++) {
            JSONObject server = (JSONObject)servers.get(i);
            
            String[] names = JSONObject.getNames(server);
            
            for (String name : names) {
                JSONArray urls = (JSONArray) server.get(name);
                if (urls.length() > 0) {
                    for (int j = 0; j < urls.length(); j++) {
                        String url = urls.get(j).toString();

                        configuration.addServerHost(name, url);
                    }
                } else {
                    configuration.addEmptyServerHost(name);
                }
            }
        }

        JSONArray containerInfo = config.getJSONArray("containerInfo");
        for (int i = 0; i < containerInfo.length(); i++) {
            JSONObject info = (JSONObject)containerInfo.get(i);

            ContainerInfo actualInfo = new ContainerInfo(info.getString("containerId"), info.getString("alias"), info.getString("releaseId"));

            configuration.addContainerInfo(actualInfo);
        }
 
        return configuration; 
    }
}
