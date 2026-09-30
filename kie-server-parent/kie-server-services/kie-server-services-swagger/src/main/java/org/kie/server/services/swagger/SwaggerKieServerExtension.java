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

package org.kie.server.services.swagger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.Severity;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;

public class SwaggerKieServerExtension implements KieServerExtension {

    public static final String EXTENSION_NAME = "Swagger";

    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_SWAGGER_SERVER_EXT_DISABLED, "false"));
    
    private KieServerRegistry context;
    
    private List<Object> services = new ArrayList<Object>();
    private boolean initialized = false;

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isActive() {
    	return disabled == false;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
    	this.context = registry;
        OpenAPI oas = new OpenAPI();
        oas.info(getInfo());

        String serverUrl = resolveServerUrl();
        if (serverUrl != null) {
            oas.addServersItem(new Server().url(serverUrl));
        }

        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
                .openAPI(oas)
                .prettyPrint(true)
                .resourcePackages(Stream.of("org.kie.server.remote.rest").collect(Collectors.toSet())); // chek packages here

        try {
            new JaxrsOpenApiContextBuilder()
                    .openApiConfiguration(oasConfig)
                    .buildContext(true);
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    	initialized = true;
    }
    
    /**
     * Resolves the base URL for the OpenAPI servers entry.
     * Prefers the fully-qualified URL derived from KIE_SERVER_LOCATION so that
     * Swagger UI generates correct curl examples including host and port.
     * Falls back to the context root path for backwards compatibility.
     */
    private String resolveServerUrl() {
        String location = System.getProperty(KieServerConstants.KIE_SERVER_LOCATION);
        if (location != null && !location.isEmpty()) {
            // KIE_SERVER_LOCATION = http://host:port/ctx/services/rest/server
            // strip the trailing /server to get the base URL
            int idx = location.lastIndexOf("/server");
            if (idx > 0) {
                return location.substring(0, idx);
            }
            return location;
        }
        String contextRoot = KieServerEnvironment.getContextRoot();
        if (contextRoot != null) {
            return contextRoot + "/services/rest";
        }
        return null;
    }

    private Info getInfo() {
    	Info info = new Info();
    	// version in general refers to major version of the project (kie server) though it uses minor as well to allow emergency type of changesinfo.setTitle("KIE-Server API");
    	// must be updated with every major release
    	info.setVersion("8.1");
    	info.setTitle("KIE Server");
    	return info;
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
    	//no-op
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
    	//no-op
    }

    @Override
    public boolean isUpdateContainerAllowed(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        return true;
    }

    @Override
    public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
    	//no-op
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        //no-op
    }
    
    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
    	
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
                = ServiceLoader.load( KieServerApplicationComponentsService.class );
        List<Object> appComponentsList = new ArrayList<Object>();
        Object[] services = {context};
        for ( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) {
            appComponentsList.addAll( appComponentsService.getAppComponents( EXTENSION_NAME, type, services ) );
        }
        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        return null;
    }

    @Override
    public String getImplementedCapability() {
        return KieServerConstants.CAPABILITY_SWAGGER;
    }

    @Override
    public List<Object> getServices() {
        return services;
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public Integer getStartOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }
   
    @Override
    public List<Message> healthCheck(boolean report) {
        List<Message> messages = KieServerExtension.super.healthCheck(report);
        
        if (report) {
            messages.add(new Message(Severity.INFO, getExtensionName() + " is alive"));
        }        
        return messages;
    }
}
