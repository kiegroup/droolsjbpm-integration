package org.drools;

import java.io.Serializable;

public class HelloWorld implements Message, Serializable {
    
    public HelloWorld() {
        
    }
    
    public String getMessage() {
        return "Hello World";
    }
}
