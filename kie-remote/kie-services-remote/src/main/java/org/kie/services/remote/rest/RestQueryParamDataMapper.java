package org.kie.services.remote.rest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.kie.api.runtime.KieSession;

public class RestQueryParamDataMapper {

    // Helper methods ------------------------------------------------------------------------------------------------------------
    
    private static Method getMethod( Class<?> serviceClass, String methodName, Class<?>... parameterTypes) {
        try { 
       return serviceClass.getMethod(methodName, parameterTypes);
        } catch( Exception e ) { 
            throw new Error("Initialization failed when getting method " + serviceClass.getSimpleName() + "." + methodName + "!");
        }
    }
    
    private static Map<String, Integer> createArgOrderMap(String... paramNames) { 
       Map<String, Integer> argOrderMap = new HashMap<String, Integer>();
        for( int i = 0; i < paramNames.length; ++i ) { 
            argOrderMap.put(paramNames[i], i);
        }
        return argOrderMap;
    }
    
    // Public methods ------------------------------------------------------------------------------------------------------------
    
    static Map<Method, Map<String, Integer>> mapKieSessionQueryParameters() {
       Map<Method, Map<String, Integer>> methodArgsMap = new HashMap<Method, Map<String,Integer>>();
       
       methodArgsMap.put(
               getMethod(KieSession.class, "startProcess", String.class ),
               createArgOrderMap("processId")
               );
    
       return methodArgsMap;
    }
    
    static Map<Method, Map<String, Integer>> mapWorkItemManagerQueryParameters() { 
       Map<Method, Map<String, Integer>> methodArgsMap = new HashMap<Method, Map<String,Integer>>();
       return methodArgsMap;
    }
    
    static Map<Method, Map<String, Integer>> mapTaskServiceQueryParameters() { 
       Map<Method, Map<String, Integer>> methodArgsMap = new HashMap<Method, Map<String,Integer>>();
       return methodArgsMap;
    }
    
}
