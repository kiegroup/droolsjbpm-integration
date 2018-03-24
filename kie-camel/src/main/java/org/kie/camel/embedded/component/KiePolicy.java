/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.camel.embedded.component;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.cxf.CxfConstants;
import org.apache.camel.component.cxf.CxfSpringEndpoint;
import org.apache.camel.model.BeanDefinition;
import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.model.MarshalDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.ToDefinition;
import org.apache.camel.model.UnmarshalDefinition;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.camel.model.dataformat.XStreamDataFormat;
import org.apache.camel.spi.Policy;
import org.apache.camel.spi.RouteContext;
import org.drools.compiler.runtime.pipeline.impl.DroolsJaxbHelperProviderImpl;
import org.drools.core.util.StringUtils;
import org.kie.api.runtime.CommandExecutor;
import org.kie.jax.soap.PostCxfSoapProcessor;
import org.kie.jax.soap.PostCxfTransportSoapProcessor;
import org.kie.jax.soap.PreCxfSoapProcessor;
import org.kie.jax.soap.PreCxfTransportSoapProcessor;

public class KiePolicy implements Policy {

    private static boolean augmented;

    // this should be the same namespace defined in META-INF/org.apache.camel.component file
    public static final String URI_PREFIX = "kie-local:";

    public void beforeWrap(RouteContext routeContext, ProcessorDefinition<?> processorDefinition) {
        augmentNodes(routeContext, processorDefinition, new HashSet<Object>());
    }

    public Processor wrap(RouteContext routeContext, Processor processor) {
        RouteDefinition routeDef = routeContext.getRoute();

        ToDefinition toKie = getKieNode(routeDef);

        Processor returnedProcessor;
        if (toKie != null) {
            returnedProcessor = new KieProcess(toKie.getUri(), processor);
        } else {
            returnedProcessor = processor;
        }
        return returnedProcessor;
    }

    private ToDefinition getKieNode(RouteDefinition routeDef) {
        ToDefinition toDrools = null;
        for (ProcessorDefinition<?> child : routeDef.getOutputs()) {
            toDrools = getKieNode(child);
            if (toDrools != null) {
                break;
            }
        }
        return toDrools;
    }

    public static void augmentNodes(RouteContext routeContext, ProcessorDefinition<?> nav, Set visited) {
        if (!nav.getOutputs().isEmpty()) {

            List<ProcessorDefinition<?>> outputs = nav.getOutputs();
            for (int i = 0; i < outputs.size(); i++) {
                ProcessorDefinition child = outputs.get(i); // it.next();
                if (child instanceof ToDefinition) {
                    ToDefinition to = (ToDefinition)child;
                    if (to.getUri().startsWith("cxfrs") && !visited.contains(to)) {
                        BeanDefinition beanDef = new BeanDefinition();
                        beanDef.setBeanType(PreCxfrs.class.getName());
                        outputs.add(i, beanDef); // insert before cxfrs
                        beanDef = new BeanDefinition();
                        beanDef.setBeanType(PostCxfrs.class.getName());
                        outputs.add(i + 2, beanDef); // insert after cxfrs
                        i = i + 2; // adjust for the two inserts
                    } else if (to.getUri().startsWith("cxf") && !visited.contains(to)) {
                        BeanDefinition beanDef = new BeanDefinition();
                        beanDef.setBeanType(PreCxfSoapProcessor.class.getName());
                        outputs.add(i, beanDef); // insert before cxf
                        beanDef = new BeanDefinition();
                        beanDef.setBeanType(PostCxfSoapProcessor.class.getName());
                        outputs.add(i + 2, beanDef); // insert after cxf
                        i = i + 2; // adjust for the two inserts
                        augmented = true;
                    }
                } else if (child instanceof MarshalDefinition) {
                    MarshalDefinition m = (MarshalDefinition)child;
                    DataFormatDefinition dformatDefinition = m.getDataFormatType();
                    dformatDefinition = processDataFormatType(routeContext, m.getRef(), dformatDefinition);
                    m.setDataFormatType(dformatDefinition); // repoint the marshaller, if it was cloned
                } else if (child instanceof UnmarshalDefinition) {
                    UnmarshalDefinition m = (UnmarshalDefinition)child;
                    DataFormatDefinition dformatDefinition = m.getDataFormatType();
                    dformatDefinition = processDataFormatType(routeContext, m.getRef(), dformatDefinition);
                    m.setDataFormatType(dformatDefinition); // repoint the marshaller, if it was cloned
                }
            }

            for (Iterator<ProcessorDefinition<?>> it = nav.getOutputs().iterator(); it.hasNext();) {
                ProcessorDefinition child = it.next();
                augmentNodes(routeContext, child, visited);
            }
        }
    }

    /** Sets the permissions on the DataFormatDefinition object in case the setPermitions method exists inside of the DataFormatDefinition class. As this KiePolicy class could
     * be used in different environments (Fuse 6.2.1, Fuse 6.3), the XStream library could not have this method in their API.
     *
     * @param dformatDefinition the data format definitoin */
    private static void setPermissions(DataFormatDefinition dformatDefinition) {
        // Use Java Reflection to get the method setPermissions. This is done to allow compatibility between fuse 6.3 and fuse 6.2.1
        // The xstream library differs on version between the 6.2.1 and 6.3 version of fuse.
        Method setPermissions = null;
        try {
            setPermissions = dformatDefinition.getClass().getMethod("setPermissions", String.class);
        } catch (Exception e) {
        }

        if (setPermissions != null) {
            try {
                setPermissions.invoke(dformatDefinition, "+*");
            } catch (Exception e) {
            }
        }
    }

    private static DataFormatDefinition processDataFormatType(RouteContext routeContext,
                                                              String ref,
                                                              DataFormatDefinition dformatDefinition) {
        if ( dformatDefinition == null ) {
            if ( "json".equals( ref ) ) {
                dformatDefinition = new XStreamDataFormat();
                ((XStreamDataFormat) dformatDefinition).setDriver("json" );
                setPermissions(dformatDefinition);
            } else if ( "xstream".equals( ref ) ) {
                dformatDefinition = new XStreamDataFormat();
                setPermissions(dformatDefinition);
            } else if ( "jaxb".equals( ref ) ) {
                dformatDefinition = new JaxbDataFormat();
            } else {
                dformatDefinition = routeContext.getCamelContext().resolveDataFormatDefinition(ref);
            }
        }
        // always clone before changing
        dformatDefinition = new FastCloner().deepClone(dformatDefinition);
        if (dformatDefinition instanceof JaxbDataFormat) {
            dformatDefinition = augmentJaxbDataFormatDefinition((JaxbDataFormat)dformatDefinition);
        } else if (dformatDefinition instanceof XStreamDataFormat) {
            XStreamDataFormat xstreamDataFormat = (XStreamDataFormat)dformatDefinition;
            if ("json".equals(xstreamDataFormat.getDriver())) {
                dformatDefinition = XStreamJson.newJSonMarshaller(xstreamDataFormat);
            } else {
                dformatDefinition = XStreamXml.newXStreamMarshaller((XStreamDataFormat)dformatDefinition);
            }
            setPermissions(dformatDefinition);
        }
        return dformatDefinition;
    }

    private ToDefinition getKieNode(ProcessorDefinition nav) {
        if (!nav.getOutputs().isEmpty()) {
            List<ProcessorDefinition<?>> children = nav.getOutputs();
            for (ProcessorDefinition child : children) {
                if (child instanceof ToDefinition) {
                    ToDefinition to = (ToDefinition)child;
                    if (to.getUri().trim().startsWith(URI_PREFIX)) {
                        return to;
                    }
                }
                getKieNode(child);
            }
        }
        return null;
    }

    /** Clones the passed JaxbDataFormat and then augments it with with Drools related namespaces
     * 
     * @param jaxbDataFormat
     * @return */
    public static JaxbDataFormat augmentJaxbDataFormatDefinition(JaxbDataFormat jaxbDataFormat) {
        Set<String> set = new HashSet<String>();

        for (String clsName : DroolsJaxbHelperProviderImpl.JAXB_ANNOTATED_CMD) {
            set.add(clsName.substring(0, clsName.lastIndexOf('.')));
        }

        StringBuilder sb = new StringBuilder();
        String contextPath = jaxbDataFormat.getContextPath();
        if (contextPath != null) {
            sb.append(contextPath);
        }
        sb.append(":");
        for (String pkgName : set) {
            sb.append(pkgName);
            sb.append(':');
        }

        jaxbDataFormat.setContextPath(sb.toString());
        return jaxbDataFormat;
    }

    public static class KieClientProcessor implements Processor {

        private Processor processor;

        public KieClientProcessor(Processor processor) {
            this.processor = processor;
        }

        public void process(Exchange exchange) throws Exception {
            exchange.setPattern(ExchangePattern.InOut);
            Message inMessage = exchange.getIn();
            inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, Boolean.TRUE);
            inMessage.setHeader(Exchange.HTTP_METHOD, "POST");
            inMessage.setHeader(Exchange.HTTP_PATH, "/execute");
            inMessage.setHeader(Exchange.ACCEPT_CONTENT_TYPE, "text/plain");
            inMessage.setHeader(Exchange.CONTENT_TYPE, "text/plain");

            this.processor.process(exchange);
        }

    }

    public static class KieProcess implements Processor {

        private String kieUri;
        private KieEmbeddedEndpoint ke;
        private Processor processor;

        public KieProcess(String kieUri, Processor processor) {
            this.kieUri = kieUri;
            this.processor = processor;
        }

        public void process(Exchange exchange) throws Exception {
            // Bad Hack - Need to remote it and fix it in Camel (if it's a camel problem)
            // I need to copy the body of the exachange because for some reason
            // the getContext().getEndpoint() erase the content/or loose the reference
            String body = exchange.getIn().getBody(String.class);
            if (ke == null) {

                this.ke = exchange.getContext().getEndpoint(this.kieUri, KieEmbeddedEndpoint.class);
            }

            if (ke == null) {
                throw new RuntimeException("Could not find DroolsEndPoint for uri=" + this.kieUri);
            }

            ClassLoader originalClassLoader = null;
            try {
                originalClassLoader = Thread.currentThread().getContextClassLoader();

                CommandExecutor exec = ke.getExecutor();
                if (exec == null) {
                    String lookup = exchange.getIn().getHeader(KieComponent.KIE_LOOKUP, String.class);
                    if (StringUtils.isEmpty(lookup)) {
                        // Bad Hack - Need to remote it and fix it in Camel (if it's a camel problem)
                        lookup = ke.getLookup(body);
                        // lookup = ke.getLookup( exchange.getIn().getBody( String.class ) );
                    }

                    if (StringUtils.isEmpty(lookup)) {
                        throw new RuntimeException("No Executor defined and no lookup information available for uri " + this.ke.getEndpointUri());
                    }
                    exec = ke.getCommandExecutor(lookup);
                }

                if (exec == null) {
                    throw new RuntimeException("CommandExecutor cannot be found for uri " + this.ke.getEndpointUri());
                }
                ClassLoader localClassLoader = ke.getClassLoader(exec);
                if (localClassLoader == null) {
                    throw new RuntimeException("CommandExecutor Classloader cannot be null for uri " + this.ke.getEndpointUri());
                }

                // Set the classloader to the one used by the CommandExecutor
                // Have to set it in both places, as not all places yet use the camel ApplicationContextClassLoader (like xstream dataformats)
                Thread.currentThread().setContextClassLoader(localClassLoader);
                exchange.getContext().setApplicationContextClassLoader(localClassLoader);

                ExecutionNodePipelineContextImpl context = new ExecutionNodePipelineContextImpl(localClassLoader);
                context.setCommandExecutor(exec);

                exchange.setProperty("kie-context", context);
                // Bad Hack - Need to remote it and fix it in Camel (if it's a camel problem)
                // I need to re set the Body because the exchange loose the content at
                // the begining of the method
                exchange.getIn().setBody(new ByteArrayInputStream(body.getBytes("UTF-8")));

                boolean soap = false;
                if (!augmented && exchange.getFromEndpoint() instanceof CxfSpringEndpoint) {
                    new PreCxfTransportSoapProcessor().process(exchange);
                    soap = true;
                }
                processor.process(exchange);
                if (soap) {
                    new PostCxfTransportSoapProcessor().process(exchange);
                }
            } finally {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
                exchange.getContext().setApplicationContextClassLoader(originalClassLoader);
            }
        }
    }

}
