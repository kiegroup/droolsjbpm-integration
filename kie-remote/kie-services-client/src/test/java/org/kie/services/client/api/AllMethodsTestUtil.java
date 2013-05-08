package org.kie.services.client.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.kie.services.client.UnfinishedTestException;

public class AllMethodsTestUtil {
    
    public static void testMethods(Object requestInstance, Method[] methods) throws Exception {
        for (Method method : methods) {
            Class unsupported = null;
            Type[] types = method.getGenericParameterTypes();
            Object[] args = new Object[types.length];
            for (int i = 0; i < args.length; ++i) {
                boolean unsupTypeFound = false;
                if (Object.class.equals(types[i])) {
                    args[i] = "testObject";
                } else if (int.class.equals(types[i])) {
                    args[i] = 23;
                } else if (long.class.equals(types[i])) {
                    args[i] = 42l;
                } else if (boolean.class.equals(types[i])) {
                    args[i] = true;
                } else if (String.class.equals(types[i])) {
                    args[i] = "testString";
                } else if (types[i] instanceof ParameterizedType) {
                    unsupTypeFound = true;
                    ParameterizedType pType = (ParameterizedType) types[i];
                    Type[] genericTypes = pType.getActualTypeArguments();
                    if (Map.class.equals(pType.getRawType())) {
                        if (String.class.equals(genericTypes[0]) && Object.class.equals(genericTypes[1])) {
                            args[i] = new HashMap<String, Object>();
                            ((Map) args[i]).put("testKey", "testVal");
                            unsupTypeFound = false;
                        }
                    }
                }
                if (unsupTypeFound) {
                    Type unsupType = types[i];
                    args[i] = null;
                    if (types[i] instanceof Class) {
                        unsupported = (Class) unsupType;
                    } else if (types[i] instanceof ParameterizedType) {
                        unsupported = (Class) ((ParameterizedType) unsupType).getRawType();
                    } else {
                        throw new UnfinishedTestException("Unknown type in unsupported method argument: " + unsupType.toString());
                    }
                }
            }

            try {
                method.invoke(requestInstance, args);
                if (unsupported != null) {
                    throw new UnfinishedTestException(unsupported.getName() + " argument not filled for supported method "
                            + method.getName());
                }
            } catch (InvocationTargetException ite) {
                if (ite.getCause() instanceof UnsupportedOperationException) {
                    System.out.println(ite.getCause().getMessage());
                }
            }
        }

    }
}