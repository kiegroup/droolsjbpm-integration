package org.kie.services.client.api.command;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.kie.api.task.TaskService;

public class TaskServiceMethodsTest {

    @Test
    public void verifyThatAllMethodsCanBeCalledTest() throws Exception {
        RemoteRuntimeEngine runtimeEngine = new RemoteRuntimeEngine(new RemoteConfiguration(RemoteConfiguration.Type.CONSTRUCTOR));
        TaskService taskService = runtimeEngine.getTaskService();

        List<Method> methods = Arrays.asList(TaskService.class.getMethods());
        Collections.sort(methods, new Comparator<Method>() {
            @Override
            public int compare( Method o1, Method o2 ) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        for( Method method : methods ) {
            if( "execute".equals(method.getName()) ) {
                continue;
            }
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] params = new Object[paramTypes.length];
            for( int i = 0; i < params.length; ++i ) {
                String typeName = paramTypes[i].getName();
                if( "long".equals(typeName) ) {
                    params[i] = 1l;
                }
                if( "int".equals(typeName) ) {
                    params[i] = 1;
                }
                if( "boolean".equals(typeName) ) {
                    params[i] = true;
                }
            }
            try {
                method.invoke(taskService, params);
            } catch( Exception e ) {
                Throwable cause = e.getCause();
                assertFalse(method.getName() + " is unsupported!", cause instanceof UnsupportedOperationException);
                if( e instanceof IllegalArgumentException ) {
                    fail("Method " + method.getName() + " needs a specific type of parameter.");
                }
                if( cause instanceof AssertionError ) {
                    continue;
                }
                throw e;
            }
        }
    }
}
