package org.drools.fluent.test.impl;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.drools.fluent.test.ReflectiveMatcher;
import org.hamcrest.Matcher;

public class ReflectiveMatcherImpl implements ReflectiveMatcher {
    private String name;
    private Object object;

    public ReflectiveMatcherImpl(String name, Object object) {
        this.name = name;
        this.object = object;
    }    

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public void build(StringBuilder sbuilder) {
        sbuilder.append( name );
        sbuilder.append( "(" );
        
        if ( object instanceof ReflectiveMatcher ) {
            // one matcher
            ((ReflectiveMatcherImpl) object).build( sbuilder );
        } else if ( object.getClass().isArray() ){
            // an array of matchers    
            ReflectiveMatcher[] array = (ReflectiveMatcher[]) object;
            
            ((ReflectiveMatcherImpl) array[0]).build( sbuilder );
            for ( int i = 1; i < array.length; i++) {
                sbuilder.append( "," );
                ((ReflectiveMatcherImpl) array[1]).build( sbuilder );
            }
        } else {
            sbuilder.append( object );
        }
        sbuilder.append( ")" );
    }

}
