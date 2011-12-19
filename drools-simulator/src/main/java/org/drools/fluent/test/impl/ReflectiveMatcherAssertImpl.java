/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.fluent.test.impl;

import java.util.HashMap;
import java.util.Map;

import org.drools.command.Context;
import org.drools.fluent.test.ReflectiveMatcher;
import org.drools.fluent.test.ReflectiveMatcherAssert;
import org.hamcrest.Matcher;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;

public class ReflectiveMatcherAssertImpl implements ReflectiveMatcherAssert {
    
    private String actual;
    
    private String text;
    
    private ReflectiveMatcherImpl matcher;
    
    private ReflectiveMatcherFactory factory;
    
    public ReflectiveMatcherAssertImpl() {
        
    }
    
    public ReflectiveMatcherAssertImpl(String  text,
                                       ReflectiveMatcherFactory factory) {
        this.text =  text;
        this.factory = factory;
    }     
    
    public ReflectiveMatcherAssertImpl(String actual,
                                       ReflectiveMatcher matcher,
                                       ReflectiveMatcherFactory factory) {
        this.actual = actual;
        this.matcher = ( ReflectiveMatcherImpl ) matcher;
        this.factory = factory;
    }   

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public ReflectiveMatcher getMatcher() {
        return matcher;
    }

    public void setReflectiveMatcher(ReflectiveMatcher matcher) {
        this.matcher = ( ReflectiveMatcherImpl ) matcher;
    }

    public <T> Matcher<T> build(Class<T> cls) {
        return null;
    }

    public void eval(Context context) {
        StringBuilder sbuilder = new StringBuilder();     
        
        for( String str : factory.getStaticImports() ) {
            sbuilder.append( "import_static " );
            sbuilder.append( str );
            sbuilder.append( ";\n" );
        }
        
        sbuilder.append(  "assertThat(" );        
        if ( text != null ) {            
            sbuilder.append( text );
        } else {
            sbuilder.append(  actual );
            sbuilder.append(  ", " );            
            matcher.build( sbuilder );
        }
        
        sbuilder.append( ")" );
        sbuilder.append( ";\n" );
      
        ParserContext pctx = new ParserContext();
        //pctx.setStrongTyping( true );
        
        String t = sbuilder.toString();
        
        //System.out.println( t );
        
        ParserContext parserCtx = new ParserContext( );
        MVEL.compileExpression( t, parserCtx );
        
        Map<String, Class> inputs = parserCtx.getInputs();
              
        Map<String, Object> vars = new HashMap<String, Object>();
      
        for ( String name : inputs.keySet() ) {
            vars.put( name, context.get( name ) );
        }     
        
        try {
            Object o = MVEL.compileExpression( sbuilder.toString(), pctx );
            MVEL.executeExpression( o, vars );
        } catch( Exception e ) {
            // Try and unwrapp to the hamcrest assertion error if we can, else rethrwo the original mvel error
            Throwable unwrapped =  e.getCause();
            if ( unwrapped != null && unwrapped.getCause() != null ) {
                unwrapped = unwrapped.getCause();
            }
            
            if ( unwrapped instanceof AssertionError) {
                throw (AssertionError)e.getCause().getCause();
            } else if ( e instanceof RuntimeException ){
                throw (RuntimeException) e;
            }
        }
    }
    
}
