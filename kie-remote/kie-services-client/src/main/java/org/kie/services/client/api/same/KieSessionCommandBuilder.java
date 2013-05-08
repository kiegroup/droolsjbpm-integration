package org.kie.services.client.api.same;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.process.AbortProcessInstanceCommand;
import org.drools.core.command.runtime.process.CreateProcessInstanceCommand;
import org.drools.core.command.runtime.process.GetProcessInstanceCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.process.StartProcessInstanceCommand;
import org.kie.services.client.api.command.CommandBuilder;
import org.kie.api.runtime.KieSession;

public class KieSessionCommandBuilder implements CommandBuilder {

    private static Map<Method, Class<? extends GenericCommand>> methodCommandClassMap 
        = new HashMap<Method, Class<? extends GenericCommand>>();
    private static Class<?> baseClass = KieSession.class;
    static { 
        addCommandClass(StartProcessCommand.class, "startProcess", String.class);
        addCommandClass(StartProcessCommand.class, "startProcess", String.class, HashMap.class);
        addCommandClass(CreateProcessInstanceCommand.class, "createProcessInstance", String.class, HashMap.class);
        addCommandClass(StartProcessInstanceCommand.class, "startProcessInstance", long.class);
        addCommandClass(SignalEventCommand.class, "signalEvent", String.class, Object.class);
        addCommandClass(SignalEventCommand.class, "signalEvent", String.class, Object.class, long.class);
        addCommandClass(GetProcessInstanceCommand.class, "getProcessInstance", long.class);
        addCommandClass(GetProcessInstanceCommand.class, "getProcessInstance", long.class, boolean.class);
        addCommandClass(AbortProcessInstanceCommand.class, "abortProcessInstance", long.class);
    }
    
    private static void addCommandClass(Class<? extends GenericCommand> cmdClazz, String methodName, Class... types) { 
        Method method;
        try {
            method = cmdClazz.getDeclaredMethod("startProcess", types);
            methodCommandClassMap.put(method, cmdClazz);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to add " + cmdClazz.getSimpleName() + " to map for method " + methodName + ": "
                    + e.getMessage(), e);
        }
    }
    
    public GenericCommand buildCommand(Method method, Object [] args) { 
        Class<? extends GenericCommand> cmdClass = methodCommandClassMap.get(method);
        
        try {
            return cmdClass.getConstructor(method.getParameterTypes()).newInstance(args);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create " + cmdClass.getSimpleName() + " for method " + method.getName() + ": "
                    + e.getMessage(), e);
        }
    }
}
