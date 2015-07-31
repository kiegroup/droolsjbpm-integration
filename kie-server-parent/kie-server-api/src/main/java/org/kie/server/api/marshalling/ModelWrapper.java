/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.api.marshalling;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.type.JaxbBoolean;
import org.kie.server.api.model.type.JaxbByte;
import org.kie.server.api.model.type.JaxbByteArray;
import org.kie.server.api.model.type.JaxbCharacter;
import org.kie.server.api.model.type.JaxbDate;
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
        wrapperPrimitives.put(byte[].class, JaxbByteArray.class);

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
        } else if (object instanceof Date) {
            return new JaxbDate((Date) object);
        } else if (object instanceof CommandScript) {
             for (KieServerCommand cmd : ((CommandScript) object).getCommands()) {
                 if (cmd instanceof DescriptorCommand) {
                     List<Object> arguments = new ArrayList<Object>();
                     for (Object o : ((DescriptorCommand) cmd).getArguments()) {
                         arguments.add(wrap(o));
                     }
                     ((DescriptorCommand) cmd).setArguments(arguments);
                 }
             }
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
