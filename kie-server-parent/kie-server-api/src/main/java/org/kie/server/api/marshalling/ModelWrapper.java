package org.kie.server.api.marshalling;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.server.api.model.type.JaxbBoolean;
import org.kie.server.api.model.type.JaxbByte;
import org.kie.server.api.model.type.JaxbCharacter;
import org.kie.server.api.model.type.JaxbDouble;
import org.kie.server.api.model.type.JaxbFloat;
import org.kie.server.api.model.type.JaxbInteger;
import org.kie.server.api.model.type.JaxbList;
import org.kie.server.api.model.type.JaxbLong;
import org.kie.server.api.model.type.JaxbMap;
import org.kie.server.api.model.type.JaxbShort;
import org.kie.server.api.model.type.JaxbString;

public class ModelWrapper {

    protected static Map<Class, Class> wrapperPrimitives = new HashMap<Class, Class>();
    static {
        wrapperPrimitives.put(Boolean.class, JaxbBoolean.class);
        wrapperPrimitives.put(Byte.class, JaxbByte.class);
        wrapperPrimitives.put(Character.class, JaxbCharacter.class);
        wrapperPrimitives.put(Short.class, JaxbShort.class);
        wrapperPrimitives.put(Integer.class, JaxbInteger.class);
        wrapperPrimitives.put(Long.class, JaxbLong.class);
        wrapperPrimitives.put(Double.class, JaxbDouble.class);
        wrapperPrimitives.put(Float.class, JaxbFloat.class);
        wrapperPrimitives.put(String.class, JaxbString.class);

    }

    public static Object wrap(Object object) {

        if (object != null && isPrimitiveOrWrapper(object.getClass())) {
            return wrapPrimitive(object);
        }

        return wrapSkipPrimitives(object);
    }

    public static Object wrapSkipPrimitives(Object object) {

         if (object instanceof List) {
            return new JaxbList((List) object);
        } else if (object instanceof Map) {
            return new JaxbMap((Map) object);
        }

        return object;
    }

    static boolean isPrimitiveOrWrapper(final Class<?> type) {
        if (type == null) {
            return false;
        }
        return type.isPrimitive() || wrapperPrimitives.containsKey(type);
    }

    static Object wrapPrimitive(final Object value) {
        try {
            Class<?> wrapperClass = wrapperPrimitives.get(value.getClass());
            Constructor c = wrapperClass.getConstructor(value.getClass());
            return c.newInstance(value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create wrapper for type " + value.getClass() + " with value " + value);
        }
    }
}
