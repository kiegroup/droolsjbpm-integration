package org.kie.services.client.api.same;

import java.lang.reflect.Method;
import java.util.Map;

import org.kie.services.client.api.AbstractServiceRequestProxy;
import org.kie.services.client.message.OperationMessage;
import org.kie.services.client.message.serialization.MessageSerializationProvider;
import org.kie.api.event.KieRuntimeEventManager;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.rule.Session;
import org.kie.api.runtime.rule.StatefulRuleSession;

class SameApiServiceRequestProxy extends AbstractServiceRequestProxy {

    static {
        try {
            Method[] moreUnsuportedMethods = { 
                    KieSession.class.getMethod("dispose", new Class[0]),
                    KieSession.class.getMethod("destroy", new Class[0]) 
            };
            for (Method unsupMethod : moreUnsuportedMethods) {
                unsupportedMethods.add(unsupMethod);
            }
        } catch (Exception e) {
            // do nothing
        }
        
        // globals, calendars, environment, kiebase 
        for(Method unsupMethod : KieRuntime.class.getDeclaredMethods() ) { 
            unsupportedMethods.add(unsupMethod);
        }
        // commands
        for(Method unsupMethod : CommandExecutor.class.getDeclaredMethods() ) { 
            unsupportedMethods.add(unsupMethod);
        }
        // Session/rules
        for(Method unsupMethod : Session.class.getMethods() ) {
            unsupportedMethods.add(unsupMethod);
        }
        for(Method unsupMethod : StatefulRuleSession.class.getMethods() ) {
            if( unsupMethod.getParameterTypes().length == 0 && "fireAllRules".equals(unsupMethod.getName()) ) { 
                continue;
            }
            unsupportedMethods.add(unsupMethod);
        }
        // TODO: add support for event listener classes with no-arg constructors
        for(Method unsupMethod : KieRuntimeEventManager.class.getMethods() ) {
            unsupportedMethods.add(unsupMethod);
        }
    }

    public SameApiServiceRequestProxy(Map<Integer, String> params, MessageSerializationProvider serializationProvider) {
        // Message
        super(params, serializationProvider);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = handleMessageHolderMethodsAndUnsupportedMethods(method, args);
        if (result != null) {
            return result;
        }

        Method[] runtimeMethods = RuntimeEngine.class.getMethods();
        for (Method possibleMethod : runtimeMethods) {
            if (method.equals(possibleMethod)) {
                Class<?> returnType = method.getReturnType();
                return returnType.cast(proxy);
            }
        }
        if( KieSession.class.getMethod("getWorkItemManager", new Class[0]).equals(method) ) { 
            return (WorkItemManager) proxy;
        }

        OperationMessage operMsg = new OperationMessage(method, args);
        this.request.addOperation(operMsg);

        return null;
    }

}