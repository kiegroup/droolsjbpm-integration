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

import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.DataFormat;
import org.drools.pipeline.camel.DroolsCamelContextInit;
import org.drools.vsm.ServiceManager;

public class DroolsProxyEndpoint extends DefaultEndpoint {

    private String id;
    private String uri;
    private String dataFormat;
    private String marshall;
    private String unmarshall;
    private RouteBuilder builder;
    
    public DroolsProxyEndpoint(String endpointUri, String remaining, DroolsComponent component) throws Exception {
        super(endpointUri, component);
        configure(component, remaining);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        throw new RuntimeCamelException("Drools consumers not supported.");
    }

    public Producer createProducer() throws Exception {
        // let's setup a route first
        // we'll come up with a better way later
        // final DataFormat xstream = new DroolsXStreamDataFormat();

        if (builder == null) {
            String smId = DroolsComponent.getSessionManagerId(uri);
            final ServiceManager sm = (ServiceManager)getCamelContext().getRegistry().lookup(smId);
            if (sm == null) {
                throw new RuntimeCamelException("Cannot find ServiceManager instance with id=\"" + 
                    smId + "\" in the CamelContext registry.");
            }

            final String inFormat = (dataFormat == null) ? unmarshall : dataFormat;
            final String outFormat = (dataFormat == null) ? marshall : dataFormat;
            builder = new RouteBuilder() {
                public void configure() throws Exception {
                    // build the route step by step
                    ProcessorDefinition<?> pipeline = from("direct:" + id).bean(new DroolsCamelContextInit(sm));
                    if (inFormat != null) {
                        pipeline = pipeline.unmarshal(inFormat);
                    }
                    pipeline = pipeline.to("drools-embedded:" + uri);
                    if (inFormat != null) {
                        pipeline = pipeline.marshal(outFormat);
                    }
                }
            };
            
            getEmbeddedContext().addRoutes(builder);
        }
        return new DroolsProxyProducer(this);
    }

    public boolean isSingleton() {
        return true;
    }

    protected void configure(DroolsComponent component, String uri) throws Exception {
        this.uri = uri;
        // create unique id for embedded route
        id = DroolsComponent.generateUuid();
    }

    @Override
    public boolean isLenientProperties(){
        return true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    public String getMarshall() {
        return marshall;
    }

    public void setMarshall(String marshall) {
        this.marshall = marshall;
    }

    public String getUnmarshall() {
        return unmarshall;
    }

    public void setUnmarshall(String unmarshall) {
        this.unmarshall = unmarshall;
    }

    public CamelContext getEmbeddedContext() {
        return ((DroolsComponent)getComponent()).getEmbeddedContext();
    }
}
