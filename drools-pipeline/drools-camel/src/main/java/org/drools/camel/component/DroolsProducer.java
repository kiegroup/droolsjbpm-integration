/*
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

package org.drools.camel.component;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.TypeConverter;
import org.apache.camel.impl.DefaultProducer;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.impl.AssignObjectAsResult;
import org.drools.runtime.pipeline.impl.ExecuteResultHandler;
import org.drools.runtime.pipeline.impl.ExecutorStage;
import org.drools.runtime.pipeline.impl.ServiceManagerPipelineContextImpl;
import org.drools.vsm.ServiceManager;
import org.w3c.dom.Document;

public class DroolsProducer extends DefaultProducer {
    private ServiceManager serviceManager;
    private DroolsConverter converter;

    public DroolsProducer(Endpoint endpoint, ServiceManager serviceManager) {
        super(endpoint);
        this.serviceManager = serviceManager;
        converter = new DroolsConverter();
    }

    public void process(Exchange exchange) throws Exception {
        ResultHandlerImpl handler = new ResultHandlerImpl();

        exchange.setProperty(DroolsEndpoint.DROOLS_CONTEXT_PROPERTY, 
            new ServiceManagerPipelineContextImpl(serviceManager, null, handler));
        
        // TypeConverter converter = exchange.getContext().getTypeConverter();
        // DroolsPayload payload = converter.convertTo(DroolsPayload.class, exchange, body);
        DroolsPayload payload = converter.toVsmPayload(exchange.getIn().getBody(Document.class), exchange);
        // The value type inside DroolsPayload is BatchExecutionImpl.  We would not need the DroolsPayload wrapper
        // if the payload would always be something like a subtype of GenericCommand.
        if (payload == null) {
            throw new RuntimeCamelException("Conversion to a drools payload type failed.");
        }
        ExecutorStage batchExecution = (ExecutorStage) PipelineFactory.newCommandExecutor();
        
        // only need to get the PipelineContext from the exchange property 
        // if we knew it could have been changed by the converter
        PipelineContext ctx = (PipelineContext)exchange.getProperty(DroolsEndpoint.DROOLS_CONTEXT_PROPERTY);
        ExecutionResults results = batchExecution.execute(payload.getValue(), ctx);
        Object xml = converter.toXmlPayload(results, exchange);
        
        AssignObjectAsResult assignResult = (AssignObjectAsResult) PipelineFactory.newAssignObjectAsResult();
        assignResult.assignResult(ctx, xml);
        ExecuteResultHandler executeResult = (ExecuteResultHandler) PipelineFactory.newExecuteResultHandler();
        executeResult.handleResult(ctx, xml);
        
        exchange.getOut().setBody(handler.getObject());
    }

    // There are nicer ways of doint this
    public static class ResultHandlerImpl implements ResultHandler {
        Object object;
        
        public void handleResult(Object object) {
            this.object = object;
        }
        
        public Object getObject() {
            return this.object;
        }
    }
}
