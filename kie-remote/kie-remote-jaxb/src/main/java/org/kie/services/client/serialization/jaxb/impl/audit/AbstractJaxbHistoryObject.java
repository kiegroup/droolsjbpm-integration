package org.kie.services.client.serialization.jaxb.impl.audit;

import static org.kie.services.client.serialization.JaxbSerializationProvider.unsupported;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class AbstractJaxbHistoryObject<T> {
    
    protected Class<? extends Object> realClass;
   
    public AbstractJaxbHistoryObject() {
        throw new UnsupportedOperationException("No-arg constructor must be implemented by the concrete class.");
    }
    
    public AbstractJaxbHistoryObject(Class<? extends Object> realClass) { 
       this.realClass = realClass; 
    }
    
    public AbstractJaxbHistoryObject(T historyObject, Class<? extends Object> objectInterface) {
        this(objectInterface);
        initialize(historyObject);
    }
    
    protected void initialize(T historyObject) {
        for (Method getIsMethod : this.realClass.getDeclaredMethods() ) { 
            String methodName = getIsMethod.getName();
            String fieldName;
            if (methodName.startsWith("get")) {
                fieldName = methodName.substring(3);
            } else if (methodName.startsWith("is")) {
                fieldName = methodName.substring(2);
            } 
            else {
                continue;
//                throw new UnsupportedOperationException("Unknown method ´" + methodName + "' in "+ this.getClass().getSimpleName() + ".");
            }
            // getField -> field (lowercase f)
            fieldName = fieldName.substring(0,1).toLowerCase() + fieldName.substring(1);
            try { 
                Field field = this.getClass().getDeclaredField(fieldName);
                boolean origAccessStatus = field.isAccessible();
                field.setAccessible(true);
                Object setObject = getIsMethod.invoke(historyObject, new Object[0]);
                field.set(this, setObject);
                field.setAccessible(origAccessStatus);
            } catch( Exception e ) { 
               throw new RuntimeException("Unable to initialize " + fieldName + " when creating " + this.getClass().getSimpleName() + ".", e ); 
            }

        }
    }
    
    public void readExternal(ObjectInput arg0) throws IOException, ClassNotFoundException {
        unsupported(realClass);
    }

    public void writeExternal(ObjectOutput arg0) throws IOException {
        unsupported(realClass);
    }

}
