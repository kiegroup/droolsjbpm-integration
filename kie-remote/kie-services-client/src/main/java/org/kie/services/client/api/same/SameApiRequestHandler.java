package org.kie.services.client.api.same;

import static org.kie.services.client.api.RemoteRuntimeEngineConstants.DOMAIN_NAME;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.kie.services.client.api.AbstractApiRequestFactoryImpl;
import org.kie.services.client.api.ApiRequestFactoryProvider;
import org.kie.services.client.api.ApiRequestFactoryProvider.RequestApiType;
import org.kie.services.client.api.MessageHolder;
import org.kie.services.client.message.ServiceMessage;
import org.kie.services.client.message.serialization.MessageSerializationProvider;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.task.TaskService;

public class SameApiRequestHandler extends AbstractApiRequestFactoryImpl {

    static { 
        ApiRequestFactoryProvider.setInstance(RequestApiType.ORIGINAL, new SameApiRequestHandler());
    }
    
    private SameApiRequestHandler() { 
        // private constructor
    }
    
    public RuntimeEngine getRemoteRuntimeEngine(String domainName, MessageSerializationProvider.Type serializationType ) {
        setSerialization(serializationType);
        return getRemoteRuntimeEngine(domainName);
    }
    
    public RuntimeEngine getRemoteRuntimeEngine(String domainName) {
        HashMap<Integer, String> params = new HashMap<Integer, String>();
        params.put(DOMAIN_NAME, domainName);
        return (RuntimeEngine) internalCreateRequestProxy(params);
    }
    
    public RuntimeEngine getRemoteRuntimeEngine(Map<Integer, String> params) {
        return (RuntimeEngine) internalCreateRequestProxy(params);
    }
    
    private Object internalCreateRequestProxy(Map<Integer, String> params) {
        if( serializationProvider == null ) { 
            throw new IllegalStateException("Serialization provider must be set before creating a request.");
        }
        Class<?>[] interfaces = { RuntimeEngine.class, KieSession.class, WorkItemManager.class, TaskService.class, MessageHolder.class };
        return Proxy.newProxyInstance(ServiceMessage.class.getClassLoader(), interfaces,
                new SameApiServiceRequestProxy(params, serializationProvider));
    }
    
  

}