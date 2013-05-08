package org.kie.services.client.api.fluent;

import static org.kie.services.client.api.RemoteRuntimeEngineConstants.DOMAIN_NAME;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.kie.services.client.api.AbstractApiRequestFactoryImpl;
import org.kie.services.client.api.ApiRequestFactoryProvider;
import org.kie.services.client.api.ApiRequestFactoryProvider.RequestApiType;
import org.kie.services.client.api.MessageHolder;
import org.kie.services.client.api.fluent.api.RemoteRuntimeEngineFluent;
import org.kie.services.client.api.fluent.api.RemoteKieSessionFluent;
import org.kie.services.client.api.fluent.api.RemoteTaskFluent;
import org.kie.services.client.message.ServiceMessage;
import org.kie.services.client.message.serialization.MessageSerializationProvider;
import org.kie.internal.fluent.runtime.WorkItemManagerFluent;

public class FluentApiRequestHandler extends AbstractApiRequestFactoryImpl {

    static { 
        ApiRequestFactoryProvider.setInstance(RequestApiType.FLUENT, new FluentApiRequestHandler());
    }
    
    private FluentApiRequestHandler() { 
        // private constructor
    }
    
    public RemoteRuntimeEngineFluent getRemoteRuntimeEngine(String domainName, MessageSerializationProvider.Type serializationType ) {
        setSerialization(serializationType);
        return getRemoteRuntimeEngine(domainName);
    }
    
    public RemoteRuntimeEngineFluent getRemoteRuntimeEngine(String domainName) {
        HashMap<Integer, String> params = new HashMap<Integer, String>();
        params.put(DOMAIN_NAME, domainName);
        return (RemoteRuntimeEngineFluent) internalCreateRequestProxy(params);
    }
    
    public RemoteRuntimeEngineFluent getRemoteRuntimeEngine(Map<Integer, String> params) {
        return (RemoteRuntimeEngineFluent) internalCreateRequestProxy(params);
    }
    
    private Object internalCreateRequestProxy(Map<Integer, String> params) {
        if( serializationProvider == null ) { 
            throw new IllegalStateException("Serialization provider must be set before creating a request.");
        }
        Class<?>[] interfaces = { RemoteRuntimeEngineFluent.class, RemoteKieSessionFluent.class, RemoteTaskFluent.class, WorkItemManagerFluent.class, MessageHolder.class };
        return Proxy.newProxyInstance(ServiceMessage.class.getClassLoader(), interfaces,
                new FluentApiServiceRequestProxy(params, serializationProvider));
    }
    
  

}