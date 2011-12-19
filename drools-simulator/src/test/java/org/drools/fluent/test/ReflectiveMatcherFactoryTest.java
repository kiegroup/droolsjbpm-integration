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

package org.drools.fluent.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.fluent.test.impl.ReflectiveMatcherFactory;
import org.drools.simulation.impl.Person;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;
public class ReflectiveMatcherFactoryTest {    
    @Test
    public void test1() {
        Person p = new Person( "yoda", 150 );
        
        assertThat( p.getName(), equalTo("yoda") );
        assertThat( p.getAge(), equalTo(150) );
        
        List<String> imports = new ArrayList<String>(); 
        imports.add( "org.junit.Assert.assertThat" );
        imports.add( "org.hamcrest.CoreMatchers.is" );
        imports.add( "org.hamcrest.CoreMatchers.equalTo" );
        
        ReflectiveMatcherFactory f = new ReflectiveMatcherFactory(imports);
        ReflectiveMatcherAssert matchAssert = f.assertThat( "p.name", f.matcher( "equalTo", "'yoda'" ) );
        
        Map vars = new HashMap();
        vars.put("p", p);
        
        //matchAssert.eval(vars);
        
        
        
        //f.matcher( "p.name", f );
        
//        String str = "import_static org.hamcrest.MatcherAssert.assertThat;\n" +
//                "import_static org.hamcrest.CoreMatchers.equalTo;\n" +
//                "\n" +
//                "assertThat( p.name, equalTo('yoda') );\n" +
//                "assertThat( p.age, equalTo(150) );\n";
//        
//        ParserContext pctx = new ParserContext();
//        pctx.addVariable( "p", Person.class );
//        //pctx.setStrongTyping( true );
//        
//        Map<String, Object> vars = new HashMap<String, Object>();
//        vars.put( "p", p);
//        
//        Object o = MVEL.compileExpression( str, pctx );
//        MVEL.executeExpression( o, vars );
        
        
    }        
}
