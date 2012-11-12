/*
 * Copyright 2010 JBoss Inc
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

package org.drools.runtime.pipeline.impl;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.ResultHandler;
import org.kie.runtime.CommandExecutor;

public class BasePipelineContext
    implements
    PipelineContext {
    private ClassLoader         classLoader;
    private Map<String, Object> properties;
    private Object              result;
    private ResultHandler       resultHandler;

    public BasePipelineContext(ClassLoader classLoader) {
        this(classLoader, null);
    }

    public BasePipelineContext(ClassLoader classLoader,
                               ResultHandler resultHandler) {
        this.classLoader = classLoader;
        this.resultHandler = resultHandler;
        this.properties = new HashMap<String, Object>();
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }
    
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public ResultHandler getResultHandler() {
        return this.resultHandler;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public CommandExecutor getCommandExecutor() {
        throw new UnsupportedOperationException( "this method is not implemented" );
    }
        
}
