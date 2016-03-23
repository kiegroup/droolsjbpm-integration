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

package org.kie.server.client.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.kie.api.command.Command;
import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.CreateContainerCommand;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.commands.GetContainerInfoCommand;
import org.kie.server.api.commands.GetScannerInfoCommand;
import org.kie.server.api.commands.GetServerInfoCommand;
import org.kie.server.api.commands.ListContainersCommand;
import org.kie.server.api.commands.UpdateReleaseIdCommand;
import org.kie.server.api.commands.UpdateScannerCommand;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesException;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.client.helper.KieServicesClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServicesClientImpl extends AbstractKieServicesClientImpl implements KieServicesClient {

    private static Logger logger = LoggerFactory.getLogger( KieServicesClientImpl.class );

    private static final ServiceLoader<KieServicesClientBuilder> clientBuilders = ServiceLoader.load(KieServicesClientBuilder.class, KieServicesClientImpl.class.getClassLoader());
    private KieServerInfo kieServerInfo;
    private Map<Class<?>, Object> servicesClients = new HashMap<Class<?>, Object>();

    public KieServicesClientImpl(KieServicesConfiguration config) {
        super(config);
        init();
    }

    public KieServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        super(config, classLoader);
        init();
    }

    private void init() {
        List<String> serverCapabilities = config.getCapabilities();

        try {
            if (serverCapabilities == null) {
                // no explicit capabilities configuration given fetch them from kieServer
                kieServerInfo = getServerInfo().getResult();
                logger.info("KieServicesClient connected to: {} version {}", kieServerInfo.getServerId(), kieServerInfo.getVersion());
                serverCapabilities = kieServerInfo.getCapabilities();
                logger.info("Supported capabilities by the server: {}", serverCapabilities);
            }
            if (serverCapabilities != null && !serverCapabilities.isEmpty()) {
                // process available client builders
                Map<String, KieServicesClientBuilder> clientBuildersByCapability = new HashMap<String, KieServicesClientBuilder>();
                for (KieServicesClientBuilder builder : clientBuilders) {
                    clientBuildersByCapability.put(builder.getImplementedCapability(), builder);
                }

                // build client based on server capabilities
                for (String capability : serverCapabilities) {
                    logger.debug("Building services client for server capability {}", capability);
                    KieServicesClientBuilder builder = clientBuildersByCapability.get(capability);

                    if (builder != null) {
                        try {
                            logger.debug("Builder '{}' for capability '{}'", builder, capability);
                            Map<Class<?>, Object> clients = builder.build(config, classLoader);

                            logger.debug("Capability implemented by {}", clients);
                            servicesClients.putAll(clients);
                        } catch (Exception e) {
                            logger.warn("Builder {} throw exception while setting up clients, no {} capabilities will be available", builder, capability);
                        }
                    } else {
                        logger.debug("No builder found for '{}' capability", capability);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Unable to connect to server to get information about it due to {}", e.getMessage());
        }
    }


    @Override
    public <T> T getServicesClient(Class<T> serviceClient) {

        if (servicesClients.containsKey(serviceClient)) {
            return (T) servicesClients.get(serviceClient);
        }

        throw new KieServicesException("Server that this client is connected to has no capabilities to handle " + serviceClient.getSimpleName());
    }

    @Override
    public ServiceResponse<KieServerInfo> getServerInfo() {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateServiceResponse( baseURI, KieServerInfo.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new GetServerInfoCommand() ) );
            return (ServiceResponse<KieServerInfo>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<KieContainerResourceList> listContainers() {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateServiceResponse( baseURI + "/containers", KieContainerResourceList.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new ListContainersCommand() ) );
            return (ServiceResponse<KieContainerResourceList>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<KieContainerResource> createContainer(String id, KieContainerResource resource) {
        if( config.isRest() ) {
            return makeHttpPutRequestAndCreateServiceResponse( baseURI + "/containers/" + id, resource, KieContainerResource.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new CreateContainerCommand( resource ) ) );
            return (ServiceResponse<KieContainerResource>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<KieContainerResource> getContainerInfo(String id) {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateServiceResponse( baseURI + "/containers/" + id, KieContainerResource.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new GetContainerInfoCommand( id ) ) );
            return (ServiceResponse<KieContainerResource>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<Void> disposeContainer(String id) {
        if( config.isRest() ) {
            return makeHttpDeleteRequestAndCreateServiceResponse( baseURI + "/containers/" + id, Void.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DisposeContainerCommand( id ) ) );
            return (ServiceResponse<Void>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponsesList executeScript(CommandScript script) {
        if( config.isRest() ) {
            return makeHttpPostRequestAndCreateCustomResponse( baseURI + "/config", script, ServiceResponsesList.class );
        } else {
            return executeJmsCommand( script );
        }
    }

    @Override
    public ServiceResponse<KieScannerResource> getScannerInfo(String id) {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateServiceResponse( baseURI + "/containers/" + id + "/scanner", KieScannerResource.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new GetScannerInfoCommand( id ) ) );
            return (ServiceResponse<KieScannerResource>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<KieScannerResource> updateScanner(String id, KieScannerResource resource) {
        if( config.isRest() ) {
            return makeHttpPostRequestAndCreateServiceResponse(
                    baseURI + "/containers/" + id + "/scanner", resource,
                    KieScannerResource.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new UpdateScannerCommand( id, resource ) ) );
            return (ServiceResponse<KieScannerResource>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<ReleaseId> updateReleaseId(String id, ReleaseId releaseId) {
        if( config.isRest() ) {
            return makeHttpPostRequestAndCreateServiceResponse(
                    baseURI + "/containers/" + id + "/release-id", releaseId,
                    ReleaseId.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new UpdateReleaseIdCommand( id, releaseId ) ) );
            return (ServiceResponse<ReleaseId>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    // for backward compatibility reason

    /**
     * This method is deprecated on KieServicesClient as it was moved to RuleServicesClient
     * @see RuleServicesClient#executeCommands(String, String)
     * @deprecated
     */
    @Deprecated
    @Override
    public ServiceResponse<String> executeCommands(String id, String payload) {
        if( config.isRest() ) {
            return makeBackwardCompatibleHttpPostRequestAndCreateServiceResponse(baseURI + "/containers/instances/" + id, payload, String.class);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new CallContainerCommand( id, payload ) ) );
            return (ServiceResponse<String>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    /**
     * This method is deprecated on KieServicesClient as it was moved to RuleServicesClient
     * @see RuleServicesClient#executeCommands(String, Command)
     * @deprecated
     */
    @Deprecated
    @Override
    public ServiceResponse<String> executeCommands(String id, Command<?> cmd) {
        if( config.isRest() ) {
            return makeBackwardCompatibleHttpPostRequestAndCreateServiceResponse(baseURI + "/containers/instances/" + id, cmd, String.class, getHeaders(cmd) );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new CallContainerCommand( id, serialize(cmd) ) ) );
            return (ServiceResponse<String>) executeJmsCommand( script, cmd.getClass().getName() ).getResponses().get( 0 );
        }
    }

    @Override
    public String toString() {
        return "KieServicesClient{" +
                "kieServer=" + kieServerInfo +
                ", available clients=" + servicesClients +
                '}';
    }
}
