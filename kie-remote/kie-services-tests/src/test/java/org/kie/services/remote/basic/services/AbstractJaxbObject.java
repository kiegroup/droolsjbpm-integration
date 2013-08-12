package org.kie.services.remote.basic.services;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AbstractJaxbObject<T> {

    protected Class<?> realClass;

    public AbstractJaxbObject() {
        throw new UnsupportedOperationException("No-arg constructor must be implemented by the concrete class.");
    }

    public AbstractJaxbObject(Class<?> realClass) {
        this.realClass = realClass;
    }

    public AbstractJaxbObject(T realObject, Class<?> objectInterface) {
        this(objectInterface);
        for (Method getIsMethod : objectInterface.getDeclaredMethods()) {
            String methodName = getIsMethod.getName();
            String fieldName;
            if (methodName.startsWith("get")) {
                fieldName = methodName.substring(3);
            } else if (methodName.startsWith("is")) {
                fieldName = methodName.substring(2);
            } else {
                continue;
            }
            // getField -> field (lowercase f)
            fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
            try {
                Field field = this.getClass().getDeclaredField(fieldName);
                boolean origAccessStatus = field.isAccessible();
                field.setAccessible(true);
                Object setObject = getIsMethod.invoke(realObject, new Object[0]);
                field.set(this, setObject);
                field.setAccessible(origAccessStatus);
            } catch (Exception e) {
                throw new RuntimeException("Unable to initialize " + fieldName + " when creating "
                        + this.getClass().getSimpleName() + ".", e);
            }

        }
    }

    public void readExternal(ObjectInput arg0) throws IOException, ClassNotFoundException {
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on the JAXB " + realClass.getSimpleName()
                + " implementation.");
    }

    public void writeExternal(ObjectOutput arg0) throws IOException {
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on the JAXB " + realClass.getSimpleName()
                + " implementation.");
    }
}
