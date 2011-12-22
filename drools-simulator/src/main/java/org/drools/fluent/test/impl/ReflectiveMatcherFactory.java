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

import java.util.ArrayList;
import java.util.List;

import org.drools.fluent.test.ReflectiveMatcher;
import org.drools.fluent.test.ReflectiveMatcherAssert;

public class ReflectiveMatcherFactory {

    private List<String> staticImports;
    
    public ReflectiveMatcherFactory(List<String> staticImports) {
        this.staticImports = staticImports;
    }
    
    public List<String> getStaticImports() {
        return staticImports;
    }
    
    public ReflectiveMatcherAssert assertThat(String string) {
        return new ReflectiveMatcherAssertImpl(string, this);
    }      

    public ReflectiveMatcherAssert assertThat(String actual, ReflectiveMatcher matcher) {
        return new ReflectiveMatcherAssertImpl(actual, matcher, this);
    }  
    
    public static ReflectiveMatcher matcher(String name, Object object) {
        return new ReflectiveMatcherImpl(name, object);
    }    
    
    public static ReflectiveMatcher matcher(String name, ReflectiveMatcher matcher) {
        return new ReflectiveMatcherImpl(name, matcher);
    }
    
    public static ReflectiveMatcher matcher( String name, ReflectiveMatcher... matchers) {
        return new ReflectiveMatcherImpl(name, matchers);
    } 
    
}
