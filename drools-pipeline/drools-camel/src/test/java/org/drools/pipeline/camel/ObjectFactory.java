package org.drools.pipeline.camel;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {
    public Person createPerson() {
        return new Person();
    }
    
    public WrappedList createWrappedList() {
        return new WrappedList();
    }
}  
