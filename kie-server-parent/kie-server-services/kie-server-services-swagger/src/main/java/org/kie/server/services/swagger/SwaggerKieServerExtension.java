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

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.DefaultJaxrsScanner;
import io.swagger.jaxrs.config.JaxrsScanner;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.jaxrs.config.SwaggerScannerLocator;
import io.swagger.models.Info;

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
    	
    	JaxrsScanner jaxrsScanner = new DefaultJaxrsScanner();
    	jaxrsScanner.setPrettyPrint(true);
    	/*
    	 * Set our JAX-RS Scanner with SCANNER_ID_DEFAULT.
    	 * We need to do this before creating the BeanConfig, as this prevents the BeanConfig to register itself as the default scanner.
    	 * The first one wins.
    	 */
    	SwaggerScannerLocator.getInstance().putScanner((SwaggerContextService.SCANNER_ID_DEFAULT), jaxrsScanner);

    	BeanConfig beanConfig = new BeanConfig();
		
    	String contextRoot = KieServerEnvironment.getContextRoot();
    	if (contextRoot != null) {
    		beanConfig.setBasePath(contextRoot + "/services/rest");
    	}
		
    	//Set the Info on the Swagger object, not on the BeanConfig ... otherwise the Info on Swagger (which will be 'null') will override the Info we set on the BeanConfig.
    	beanConfig.getSwagger().setInfo(getInfo());
    	beanConfig.setScan(true);
		
    	initialized = true;
    }
    
    private Info getInfo() {
    	Info info = new Info();
    	// version in general refers to major version of the project (kie server) though it uses minor as well to allow emergency type of changesinfo.setTitle("KIE-Server API");
    	// must be updated with every major release
    	info.setVersion("7.0");
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
