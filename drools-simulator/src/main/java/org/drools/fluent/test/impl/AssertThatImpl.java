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

import org.drools.fluent.test.ReflectiveMatcher;
import org.hamcrest.Matcher;

public class AssertThatImpl implements ReflectiveMatcher {
    
    private String name;
    
    private Object object;
    
    public AssertThatImpl() {
        
    }
    
    public AssertThatImpl(String name,
                                 Object object) {
        super();
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

    public <T> Matcher<T> build(Class<T> cls) {
        return null;
    }
    
}
