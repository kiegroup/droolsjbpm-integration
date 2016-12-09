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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.kie.server.router.Configuration;

public class ConfigurationMarshaller {

    public String marshall(Configuration configuration) throws Exception {
        Map<String, Set<String>> perContainer = configuration.getHostsPerContainer();
        Map<String, Set<String>> perServer = configuration.getHostsPerServer();
        
        JSONArray servers = new JSONArray();
        JSONArray containers = new JSONArray();
        JSONObject config = new JSONObject();            
        
        for (Entry<String, Set<String>> entry : perContainer.entrySet()) {
            JSONArray array = new JSONArray();
            entry.getValue().forEach(url -> array.put(url));
            
            JSONObject container = new JSONObject();
            container.put(entry.getKey(), array);
            
            containers.put(container);
        }
        
        for (Entry<String, Set<String>> entry : perServer.entrySet()) {
            JSONArray array = new JSONArray();
            entry.getValue().forEach(url -> array.put(url));
            
            JSONObject server = new JSONObject();
            server.put(entry.getKey(), array);
            
            servers.put(server);
        }
        
        config.put("containers", containers);
        config.put("servers", servers);
        
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
                
                for (int j = 0; j < urls.length(); j++) {
                    String url = urls.get(j).toString();
                    
                    configuration.addContainerHost(name, url);
                }
            }
        }
        
        JSONArray servers = config.getJSONArray("servers");
        for (int i = 0; i < servers.length(); i++) {
            JSONObject server = (JSONObject)servers.get(i);
            
            String[] names = JSONObject.getNames(server);
            
            for (String name : names) {
                JSONArray urls = (JSONArray) server.get(name);
                
                for (int j = 0; j < urls.length(); j++) {
                    String url = urls.get(j).toString();
                    
                    configuration.addServerHost(name, url);
                }
            }
        }
 
        return configuration; 
    }
}
