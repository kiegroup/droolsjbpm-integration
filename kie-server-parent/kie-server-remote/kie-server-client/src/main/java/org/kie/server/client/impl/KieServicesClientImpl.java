/*
 * Copyright 2015 - 2017 Red Hat, Inc. and/or its affiliates.
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

import org.kie.api.command.Command;
import org.kie.server.api.commands.GetReleaseIdCommand;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.CreateContainerCommand;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.commands.GetContainerInfoCommand;
import org.kie.server.api.commands.GetScannerInfoCommand;
import org.kie.server.api.commands.GetServerInfoCommand;
import org.kie.server.api.commands.GetServerStateCommand;
import org.kie.server.api.commands.ListContainersCommand;
import org.kie.server.api.commands.UpdateReleaseIdCommand;
import org.kie.server.api.commands.UpdateScannerCommand;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServerStateInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesException;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.client.helper.KieServicesClientBuilder;
import org.kie.server.client.jms.RequestReplyResponseHandler;
import org.kie.server.client.jms.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public class KieServicesClientImpl extends AbstractKieServicesClientImpl implements KieServicesClient {

    private static Logger logger = LoggerFactory.getLogger(KieServicesClientImpl.class);

    private static final ServiceLoader<KieServicesClientBuilder> clientBuilders = ServiceLoader.load(KieServicesClientBuilder.class, KieServicesClientImpl.class.getClassLoader());
    private static List<KieServicesClientBuilder> loadedClientBuilders = loadClientBuilders();   // load it only once to make sure it's thread safe

    private String conversationId;

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
        setOwner(this);
        List<String> serverCapabilities = config.getCapabilities();

        if (serverCapabilities == null) {
            serverCapabilities = getCapabilitiesFromServer();
        }
        if (serverCapabilities != null && !serverCapabilities.isEmpty()) {
            // process available client builders
            Map<String, KieServicesClientBuilder> clientBuildersByCapability = new HashMap<String, KieServicesClientBuilder>();
            for (KieServicesClientBuilder builder : loadedClientBuilders) {
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

                        for (Object serviceClient : clients.values()) {
                            if (serviceClient instanceof AbstractKieServicesClientImpl) {
                                ((AbstractKieServicesClientImpl) serviceClient).setOwner(this);
                            }
                        }

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

    }

    private List<String> getCapabilitiesFromServer() {
        ResponseHandler responseHandler = getResponseHandler();
        if (config.isJms() && !(responseHandler instanceof RequestReplyResponseHandler)) {
            setResponseHandler(new RequestReplyResponseHandler());
            kieServerInfo = getServerInfo().getResult();
            setResponseHandler(responseHandler);
        } else {
            kieServerInfo = getServerInfo().getResult();
        }

        logger.debug("KieServicesClient connected to: {} version {}", kieServerInfo.getServerId(), kieServerInfo.getVersion());
        List<String> capabilities = kieServerInfo.getCapabilities();
        logger.debug("Supported capabilities by the server: {}", capabilities);

        return capabilities;
    }

    private KieServerInfo getServerInfoInTransaction() {
        throw new IllegalStateException("Cannot get capabilities from server in a transaction");
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
            return makeHttpGetRequestAndCreateServiceResponse(loadBalancer.getUrl(), KieServerInfo.class );
        } else {
            CommandScript script = new CommandScript(Collections.singletonList((KieServerCommand) new GetServerInfoCommand()));
            ServiceResponse<KieServerInfo> response = (ServiceResponse<KieServerInfo>) executeJmsCommand(script).getResponses().get(0);
            return getResponseOrNullIfNoResponse(response);
        }
    }

    @Override
    public ServiceResponse<KieContainerResourceList> listContainers() {
        return listContainers(KieContainerResourceFilter.ACCEPT_ALL);
    }

    @Override
    public ServiceResponse<KieContainerResourceList> listContainers(KieContainerResourceFilter containerFilter) {
        if (config.isRest()) {
            String queryParams = containerFilter.toURLQueryString();
            String uri = loadBalancer.getUrl() + "/containers" + (queryParams.isEmpty() ? "" : "?" + queryParams);
            return makeHttpGetRequestAndCreateServiceResponse(uri, KieContainerResourceList.class);
        } else {
            CommandScript script = new CommandScript(Collections.singletonList((KieServerCommand) new ListContainersCommand(containerFilter)));
            ServiceResponse<KieContainerResourceList> response = (ServiceResponse<KieContainerResourceList>) executeJmsCommand(script).getResponses().get(0);
            return getResponseOrNullIfNoResponse(response);
        }
    }

    @Override
    public ServiceResponse<KieContainerResource> createContainer(String id, KieContainerResource resource) {
        if( config.isRest() ) {
            return makeHttpPutRequestAndCreateServiceResponse( loadBalancer.getUrl() + "/containers/" + id, resource, KieContainerResource.class );
        } else {
            CommandScript script = new CommandScript(Collections.singletonList((KieServerCommand) new CreateContainerCommand(resource)));
            ServiceResponse<KieContainerResource> response = (ServiceResponse<KieContainerResource>) executeJmsCommand(script, null, null, id).getResponses().get(0);
            return getResponseOrNullIfNoResponse(response);
        }
    }

    @Override
    public ServiceResponse<KieContainerResource> getContainerInfo(String id) {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateServiceResponse( loadBalancer.getUrl() + "/containers/" + id, KieContainerResource.class );
        } else {
            CommandScript script = new CommandScript(Collections.singletonList((KieServerCommand) new GetContainerInfoCommand(id)));
            ServiceResponse<KieContainerResource> response = (ServiceResponse<KieContainerResource>) executeJmsCommand(script, null, null, id).getResponses().get(0);
            return getResponseOrNullIfNoResponse(response);
        }
    }

    @Override
    public ServiceResponse<Void> disposeContainer(String id) {
        if( config.isRest() ) {
            return makeHttpDeleteRequestAndCreateServiceResponse( loadBalancer.getUrl() + "/containers/" + id, Void.class );
        } else {
            CommandScript script = new CommandScript(Collections.singletonList((KieServerCommand) new DisposeContainerCommand(id)));
            ServiceResponse<Void> response = (ServiceResponse<Void>) executeJmsCommand(script, null, null, id).getResponses().get(0);
            return getResponseOrNullIfNoResponse(response);
        }
    }

    @Override
    public ServiceResponsesList executeScript(CommandScript script) {
        if( config.isRest() ) {
            return makeHttpPostRequestAndCreateCustomResponse( loadBalancer.getUrl() + "/config", script, ServiceResponsesList.class );
        } else {
            ServiceResponsesList responsesList = executeJmsCommand(script);
            if (!responsesList.getResponses().isEmpty() && shouldReturnWithNullResponse(responsesList.getResponses().get(0))) {
                return null;
            }
            return responsesList;
        }
    }

    @Override
    public ServiceResponse<KieScannerResource> getScannerInfo(String id) {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateServiceResponse( loadBalancer.getUrl() + "/containers/" + id + "/scanner", KieScannerResource.class );
        } else {
            CommandScript script = new CommandScript(Collections.singletonList((KieServerCommand) new GetScannerInfoCommand(id)));
            ServiceResponse<KieScannerResource> response = (ServiceResponse<KieScannerResource>) executeJmsCommand(script, null, null, id).getResponses().get(0);
            return getResponseOrNullIfNoResponse(response);
        }
    }

    @Override
    public ServiceResponse<KieScannerResource> updateScanner(String id, KieScannerResource resource) {
        if (config.isRest()) {
            return makeHttpPostRequestAndCreateServiceResponse(
                    loadBalancer.getUrl() + "/containers/" + id + "/scanner", resource,
                    KieScannerResource.class );
        } else {
            CommandScript script = new CommandScript(Collections.singletonList((KieServerCommand) new UpdateScannerCommand(id, resource)));
            ServiceResponse<KieScannerResource> response = (ServiceResponse<KieScannerResource>) executeJmsCommand(script, null, null, id).getResponses().get(0);
            return getResponseOrNullIfNoResponse(response);
        }
    }

    @Override
    public ServiceResponse<ReleaseId> getReleaseId(String containerId) {
        if (config.isRest()) {
            return makeHttpGetRequestAndCreateServiceResponse(loadBalancer.getUrl() + "/containers/" + containerId + "/release-id", ReleaseId.class);
        } else {
            CommandScript script = new CommandScript(Collections.singletonList(new GetReleaseIdCommand(containerId)));
            ServiceResponse<ReleaseId> response = (ServiceResponse<ReleaseId>) executeJmsCommand(script).getResponses().get(0);
            return getResponseOrNullIfNoResponse(response);
        }
    }

    @Override
    public ServiceResponse<ReleaseId> updateReleaseId(String id, ReleaseId releaseId) {
        if (config.isRest()) {
            return makeHttpPostRequestAndCreateServiceResponse(
                    loadBalancer.getUrl() + "/containers/" + id + "/release-id", releaseId,
                    ReleaseId.class );
        } else {
            CommandScript script = new CommandScript(Collections.singletonList((KieServerCommand) new UpdateReleaseIdCommand(id, releaseId)));
            ServiceResponse<ReleaseId> response = (ServiceResponse<ReleaseId>) executeJmsCommand(script, null, null, id).getResponses().get(0);
            return getResponseOrNullIfNoResponse(response);
        }
    }

    @Override
    public ServiceResponse<KieServerStateInfo> getServerState() {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateServiceResponse(loadBalancer.getUrl() + "/state", KieServerStateInfo.class);
        } else {
            CommandScript script = new CommandScript(Collections.singletonList((KieServerCommand) new GetServerStateCommand()));
            ServiceResponse<KieServerStateInfo> response = (ServiceResponse<KieServerStateInfo>) executeJmsCommand(script).getResponses().get(0);
            return getResponseOrNullIfNoResponse(response);
        }
    }

    // for backward compatibility reason

    /**
     * This method is deprecated on KieServicesClient as it was moved to RuleServicesClient
     *
     * @see RuleServicesClient#executeCommands(String, String)
     * @deprecated
     */
    @Deprecated
    @Override
    public ServiceResponse<String> executeCommands(String id, String payload) {
        if( config.isRest() ) {
            return makeBackwardCompatibleHttpPostRequestAndCreateServiceResponse(loadBalancer.getUrl() + "/containers/instances/" + id, payload, String.class);
        } else {
            CommandScript script = new CommandScript(Collections.singletonList((KieServerCommand) new CallContainerCommand(id, payload)));
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand(script, null, null, id).getResponses().get(0);
            return getResponseOrNullIfNoResponse(response);
        }
    }

    /**
     * This method is deprecated on KieServicesClient as it was moved to RuleServicesClient
     *
     * @see RuleServicesClient#executeCommands(String, Command)
     * @deprecated
     */
    @Deprecated
    @Override
    public ServiceResponse<String> executeCommands(String id, Command<?> cmd) {
        if( config.isRest() ) {
            return makeBackwardCompatibleHttpPostRequestAndCreateServiceResponse(loadBalancer.getUrl() + "/containers/instances/" + id, cmd, String.class, getHeaders(cmd) );
        } else {
            CommandScript script = new CommandScript(Collections.singletonList((KieServerCommand) new CallContainerCommand(id, serialize(cmd))));
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand(script, cmd.getClass().getName(), null, id).getResponses().get(0);
            return getResponseOrNullIfNoResponse(response);
        }
    }

    @Override
    public String toString() {
        return "KieServicesClient{" +
                "kieServer=" + kieServerInfo +
                ", available clients=" + servicesClients +
                '}';
    }


    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.marshaller.setClassLoader(classLoader);
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.marshaller.getClassLoader();
    }

    @Override
    public String getConversationId() {
        return conversationId;
    }

    @Override
    public void completeConversation() {
        conversationId = null;
    }

    public void setConversationId(String conversationId) {
        if (conversationId != null) {
            this.conversationId = conversationId;
        }
    }

    private static synchronized List<KieServicesClientBuilder> loadClientBuilders() {
        List<KieServicesClientBuilder> builders = new ArrayList<KieServicesClientBuilder>();
        for (KieServicesClientBuilder builder : clientBuilders) {
            builders.add(builder);
        }

        return builders;
    }

    private <T> ServiceResponse<T> getResponseOrNullIfNoResponse(ServiceResponse<T> response) {
        if (shouldReturnWithNullResponse(response)) {
            return null;
        }
        return response;
    }

}
