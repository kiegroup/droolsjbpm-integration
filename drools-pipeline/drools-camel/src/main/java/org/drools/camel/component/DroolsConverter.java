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

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.help.BatchExecutionHelper;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.impl.XStreamFromXmlVsmTransformer;
import org.drools.runtime.pipeline.impl.XStreamResolverStrategy;
import org.drools.runtime.pipeline.impl.XStreamToXmlVsmTransformer;
import org.w3c.dom.Document;

import com.thoughtworks.xstream.XStream;

@Converter
public final class DroolsConverter {
    private XStreamFromXmlVsmTransformer inTransformer;
    private XStreamToXmlVsmTransformer outTransformer;

    public DroolsConverter() {
        // The XStreamFromXmlVsmTransformer will throw an IllegalArgumentException if lookup is null
        // this *will* be the case if the ksession is specified in the url, so we need a different 
        // kind of transformer there.
        inTransformer = new XStreamFromXmlVsmTransformer(new XStreamResolverStrategy() {
                public XStream lookup(String name) {
                    return BatchExecutionHelper.newXStreamMarshaller();
                }
            });
        outTransformer = new XStreamToXmlVsmTransformer();
    }

    @Converter
    public DroolsPayload toVsmPayload(Document payload, Exchange exchange) {
    	PipelineContext context = (PipelineContext)exchange.getProperty(DroolsEndpoint.DROOLS_CONTEXT_PROPERTY);
    	// check for null context and throw CamelRuntimeException?
    	inTransformer.processPayload(payload, context);
    	// this was done in the initial example, is it really necessary? why reset the context?
        exchange.setProperty(DroolsEndpoint.DROOLS_CONTEXT_PROPERTY, inTransformer.getContext());
        return new DroolsPayload(inTransformer.getPayload());
    }

    @Converter
    public Object toXmlPayload(ExecutionResults payload, Exchange exchange) {
        return outTransformer.transform((PipelineContext)exchange.getProperty(DroolsEndpoint.DROOLS_CONTEXT_PROPERTY), payload);
    }
}
