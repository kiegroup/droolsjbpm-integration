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

package org.drools.camel.component;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.io.Resource;
import org.drools.reteoo.ReteooRuleBase;
import org.drools.runtime.CommandExecutor;

import org.drools.runtime.help.BatchExecutionHelper;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.impl.ServiceManagerPipelineContextImpl;
import org.drools.runtime.pipeline.impl.XStreamFromXmlVsmTransformer;
import org.drools.runtime.pipeline.impl.XStreamResolverStrategy;
import org.drools.vsm.ServiceManager;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author salaboy
 */
public class DroolsXStreamDataFormat implements DataFormat{
    private XStreamResolverStrategy xstreamStrategy = null;
    private final XStreamFromXmlVsmTransformer transformer;
    private String charset;
    public DroolsXStreamDataFormat() {
          xstreamStrategy = new XStreamResolverStrategy() {

            public XStream lookup(String name) {
                return BatchExecutionHelper.newXStreamMarshaller();
            }
        };
        this.transformer = new XStreamFromXmlVsmTransformer(xstreamStrategy);
    }



    public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {

        PipelineContext context = (PipelineContext) exchange.getProperty("drools-context");
        XStream xstream = (XStream) context.getProperties().get( "xstream-instance" );
        xstream.setClassLoader(context.getClassLoader());
        String result = null;
        try {
            result = xstream.toXML(exchange.getIn().getBody());
        } catch (Exception e) {
            //handleException(this, object, e);
            e.printStackTrace();
        }

        byte[] bytes;
        if (charset != null) {
            bytes = result.getBytes(charset);
        } else {
            bytes = result.getBytes();
        }

        stream.write(bytes);

            
    }

    public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {

        //        this.transformer.processPayload(exchange.getIn().getBody(), (PipelineContext)exchange.getProperty("drools-context"));
//        exchange.getOut().setBody(this.transformer.getPayload());
//        exchange.setProperty("drools-context",this.transformer.getContext() );


            PipelineContext context = (PipelineContext) exchange.getProperty("drools-context");
            //Document d =  processTranslate(exchange.getIn().getBody());
            Document d =  exchange.getIn().getBody(Document.class);
            String name = d.getDocumentElement().getAttribute("lookup");
            XStream xstream = this.xstreamStrategy.lookup(name);
            if (xstream == null) {
                throw new IllegalArgumentException("Unable to lookup XStream parser using name '" + name + "'");
            }
            ServiceManagerPipelineContextImpl vsmContext = (ServiceManagerPipelineContextImpl) exchange.getProperty("drools-context");
            ServiceManager sm = vsmContext.getServiceManager();
            CommandExecutor executor = sm.lookup(name);
            if (executor == null) {
                throw new IllegalArgumentException("Unable to lookup CommandExecutor using name '" + name + "'");
            }
            vsmContext.setCommandExecutor(executor);
            ClassLoader cl = null;
            if (executor instanceof StatefulKnowledgeSessionImpl) {
                cl = ((ReteooRuleBase) (( StatefulKnowledgeSessionImpl ) executor).getRuleBase()).getRootClassLoader();
                xstream.setClassLoader(cl);
            } else if (executor instanceof StatelessKnowledgeSessionImpl) {
                cl = ((ReteooRuleBase) (( StatelessKnowledgeSessionImpl ) executor).getRuleBase()).getRootClassLoader();
            } else {
                throw new IllegalArgumentException("Unable to set ClassLoader on " + executor);
            }
            xstream.setClassLoader(cl);
            vsmContext.setClassLoader(cl);
            Object payload = xstream.unmarshal(new DomReader(d));

            context.getProperties().put("xstream-instance", xstream);
            exchange.setProperty("drools-context", context);
            return payload;
    }
     private Document processTranslate(Object object) {
        // Create a DOM builder and parse the fragment
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document d = null;
        try {
            d = factory.newDocumentBuilder().parse((InputSource) null);
        } catch (Exception e) {
        }
        try {
            if (object instanceof String) {
                d = factory.newDocumentBuilder().parse(new InputSource(new StringReader((String) object)));
            } else if (object instanceof Reader) {
                d = factory.newDocumentBuilder().parse(new InputSource((Reader) object));
            } else if (object instanceof InputStream) {
                d = factory.newDocumentBuilder().parse((InputStream) object);
            } else if (object instanceof Resource) {
                d = factory.newDocumentBuilder().parse(new InputSource(((Resource) object).getReader()));
            } else {
                throw new IllegalArgumentException("signal object must be instance of InputStream or Resource");
            }
        } catch (Exception e) {
            //handleException(this, object, e);
            e.printStackTrace();
        }
        return d;
    }


}
