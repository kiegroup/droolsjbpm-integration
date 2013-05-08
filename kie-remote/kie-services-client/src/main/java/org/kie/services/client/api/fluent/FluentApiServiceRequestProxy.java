package org.kie.services.client.api.fluent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.kie.services.client.api.AbstractServiceRequestProxy;
import org.kie.services.client.api.fluent.api.RemoteRuntimeEngineFluent;
import org.kie.services.client.api.fluent.api.RemoteRuntimeEngineFluentReturner;
import org.kie.services.client.message.OperationMessage;
import org.kie.services.client.message.ServiceMessageMapper;
import org.kie.services.client.message.serialization.MessageSerializationProvider;
import org.kie.internal.fluent.runtime.KieSessionFluent;
import org.kie.internal.fluent.runtime.WorkItemManagerFluent;

class FluentApiServiceRequestProxy extends AbstractServiceRequestProxy {

    static {
        // add unsupported?
//        for(Method unsupMethod : KieRuntime.class.getDeclaredMethods() ) { 
//            unsupportedMethods.add(unsupMethod);
//        }
    }

    public FluentApiServiceRequestProxy(Map<Integer, String> params, MessageSerializationProvider serializationProvider) {
        // Message
        super(params, serializationProvider);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = handleMessageHolderMethodsAndUnsupportedMethods(method, args);
        if (result != null) {
            return result;
        }

        List<Method> returnMethods = new ArrayList<Method>();
        returnMethods.addAll(Arrays.asList(RemoteRuntimeEngineFluent.class.getMethods()));
        returnMethods.addAll(Arrays.asList(RemoteRuntimeEngineFluentReturner.class.getMethods()));
        for (Method possibleMethod : returnMethods) {
            if (method.equals(possibleMethod)) {
                Class<?> returnType = method.getReturnType();
                return returnType.cast(proxy);
            }
        }
        if( KieSessionFluent.class.getMethod("getWorkItemManager", new Class[0]).equals(method) ) { 
            return (WorkItemManagerFluent) proxy;
        }

        // TODO: support for .set(...) and .out()
        
        OperationMessage operMsg = convertToOperationMessage(method, args);
        this.request.addOperation(operMsg);

        return proxy;
    }

    public OperationMessage convertToOperationMessage(Method method, Object [] args) {
        throw new UnsupportedOperationException("Not finished yet.");
    }
}