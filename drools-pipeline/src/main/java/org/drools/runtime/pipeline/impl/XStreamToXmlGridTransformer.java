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

import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Transformer;

import com.thoughtworks.xstream.XStream;

public class XStreamToXmlGridTransformer extends BaseEmitter
    implements
    Transformer {
    Object result = null;
    public void receive(Object object,
                        PipelineContext context) { 
        this.result = transform(context, object);
        
        emit( result,
              context );
    }

    public Object transform(PipelineContext context, Object object) {
        XStream xstream = (XStream) context.getProperties().get( "xstream-instance" );
        xstream.setClassLoader(context.getClassLoader());
        Object result = null;
        try {
            result = xstream.toXML(object);
        } catch (Exception e) {
            handleException(this, object, e);
        }
        return result;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
    
}
