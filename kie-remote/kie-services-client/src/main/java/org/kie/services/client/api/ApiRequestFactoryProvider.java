package org.kie.services.client.api;

import java.util.HashMap;

import org.kie.services.client.api.fluent.FluentApiRequestHandler;
import org.kie.services.client.api.same.SameApiRequestHandler;

public class ApiRequestFactoryProvider {
    
    private static HashMap<RequestApiType, Object> instanceMap = new HashMap<ApiRequestFactoryProvider.RequestApiType, Object>();
    
    public static SameApiRequestHandler createNewSameApiInstance() { 
        loadClass(SameApiRequestHandler.class);
        return (SameApiRequestHandler) instanceMap.get(RequestApiType.ORIGINAL);
    }

    public static FluentApiRequestHandler createNewFluentApiInstance() { 
        loadClass(FluentApiRequestHandler.class);
        return (FluentApiRequestHandler) instanceMap.get(RequestApiType.FLUENT);
    }
    
    private static void loadClass(Class clazz) {
        try {
            Class.forName(clazz.getName(), true, clazz.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public static void setInstance(RequestApiType apiType, Object object) { 
        instanceMap.put(apiType, object);
    }
    

    public enum RequestApiType {
        ORIGINAL, FLUENT;
    }
}
