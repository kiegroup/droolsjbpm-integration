/*
 *  Copyright 2009 salaboy.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.drools.pipeline.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.Receiver;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.StageExceptionHandler;
import org.drools.runtime.pipeline.impl.ServiceManagerPipelineContextImpl;
import org.drools.runtime.pipeline.impl.XStreamResolverStrategy;
import org.drools.vsm.ServiceManager;

/**
 *
 * @author salaboy
 */
public class CamelServiceManagerPipelineImpl implements Pipeline {

    private ServiceManager sm;
    private CamelContext camelContext;
    private ProducerTemplate template;
    

    public CamelServiceManagerPipelineImpl(ServiceManager sm, CamelContext context) {
        this.sm = sm;
        this.camelContext = context;
        this.template = camelContext.createProducerTemplate();

    }


    public void startCamel() throws Exception{
        this.camelContext.start();
        this.template.start();
    }

    public void insert(Object object, ResultHandler resultHandler) {

        template.sendBodyAndProperty("direct:start", (String)object,"drools-context",new ServiceManagerPipelineContextImpl(sm,null,resultHandler));
    }

    public void setStageExceptionHandler(StageExceptionHandler exceptionHandler) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setReceiver(Receiver receiver) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Receiver getReceiver() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }
    
}
