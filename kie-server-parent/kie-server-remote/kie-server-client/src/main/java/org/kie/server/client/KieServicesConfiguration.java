/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.balancer.LoadBalancer;
import org.kie.server.client.jms.ResourcesCache;
import org.kie.server.client.jms.ResponseHandler;

public interface KieServicesConfiguration {
    public static enum Transport {
        REST, JMS;
    }

    String getServerUrl();

    KieServicesConfiguration setServerUrl(String url);

    String getUserName();

    KieServicesConfiguration setUserName(String userName);

    String getPassword();

    KieServicesConfiguration setPassword(String password);

    MarshallingFormat getMarshallingFormat();

    KieServicesConfiguration setMarshallingFormat(MarshallingFormat format);

    boolean isJms();

    boolean isRest();

    Set<Class<?>> getExtraClasses();

    boolean addExtraClasses(Set<Class<?>> extraClassList);

    KieServicesConfiguration setExtraClasses(Set<Class<?>> extraClasses);

    KieServicesConfiguration clearExtraClasses();

    Transport getTransport();

    long getTimeout();

    KieServicesConfiguration setTimeout(long timeout);

    boolean getUseUssl();

    KieServicesConfiguration setUseSsl(boolean useSsl);

    KieServicesConfiguration setRemoteInitialContext(InitialContext context);

    ConnectionFactory getConnectionFactory();

    KieServicesConfiguration setConnectionFactory(ConnectionFactory connectionFactory);

    Queue getRequestQueue();

    KieServicesConfiguration setRequestQueue(Queue requestQueue);

    Queue getResponseQueue();
    
    ResourcesCache getResources();

    KieServicesConfiguration setResponseQueue(Queue responseQueue);

    void dispose();

    KieServicesConfiguration clone();

    void setCapabilities(List<String> capabilities);

    List<String> getCapabilities();

    void setCredentialsProvider(CredentialsProvider credentialsProvider);

    CredentialsProvider getCredentialsProvider();

    void setLoadBalancer(LoadBalancer loadBalancer);

    LoadBalancer getLoadBalancer();

    void setResponseHandler(ResponseHandler responseHandler);

    ResponseHandler getResponseHandler();

    void setJmsTransactional(boolean transacted);

    boolean isJmsTransactional();

    void setHeaders(Map<String, String> headers);

    Map<String, String> getHeaders();

    /**
     * Deprecated use #getExtraClasses instead
     */
    @Deprecated
    Set<Class<?>> getExtraJaxbClasses();

    /**
     * Deprecated use #addExtraClasses instead
     */
    @Deprecated
    boolean addJaxbClasses(Set<Class<?>> extraJaxbClassList);

    /**
     * Deprecated use #setExtraClasses instead
     */
    @Deprecated
    KieServicesConfiguration setExtraJaxbClasses(Set<Class<?>> extraJaxbClasses);

    /**
     * Deprecated use #clearExtraClasses instead
     */
    @Deprecated
    KieServicesConfiguration clearJaxbClasses();

}
