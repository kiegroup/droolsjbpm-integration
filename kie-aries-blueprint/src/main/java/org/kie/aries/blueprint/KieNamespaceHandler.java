/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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
package org.kie.aries.blueprint;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.apache.aries.blueprint.mutable.MutablePassThroughMetadata;
import org.kie.aries.blueprint.namespace.*;
import org.osgi.service.blueprint.container.ComponentDefinitionException;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KieNamespaceHandler implements org.apache.aries.blueprint.NamespaceHandler {

    /** The list of Aries Blueprint XML files*/
    protected java.util.List<java.net.URL> resources;

    /** The Drools Aries Blueprint namespace */
    public static final String NS_URI = "http://drools.org/schema/kie-aries-blueprint/1.0.0";
    /** The standard blueprint namespace */
    private static final String BLUEPRINT_NS = "http://www.osgi.org/xmlns/blueprint/v1.0.0";

    public static final String ELEMENT_RELEASE_ID = "releaseId";
    public static final String ELEMENT_KBASE_REF = "kbase-ref";
    public static final String ELEMENT_KBASE = "kbase";
    public static final String ELEMENT_KCONTAINER = "kcontainer-ref";
    public static final String ELEMENT_KSTORE = "kstore";
    public static final String ELEMENT_KSESSION = "ksession";
    public static final String ELEMENT_KSESSION_REF = "ksession-ref";
    public static final String ELEMENT_EVENT_LISTENERS = "eventListeners";
    public static final String ELEMENT_ENVIRONMENT = "environment";
    public static final String ELEMENT_KRUNTIMEMANAGER = "kruntimeManager";
    public static final String ELEMENT_KSESSION_RUNTIMEMANAGER = "kruntimeManagerSession";
    public static final String ELEMENT_KMODULE = "kmodule";

    protected static Map<String, AbstractElementParser> droolsElementParserMap = new HashMap<String, AbstractElementParser>();
    static {
        droolsElementParserMap.put(ELEMENT_RELEASE_ID, new ReleaseIdElementParser());
        droolsElementParserMap.put(ELEMENT_KBASE_REF, new KieBaseElementParser());
        droolsElementParserMap.put(ELEMENT_KBASE, new KieBaseElementParser());
        droolsElementParserMap.put(ELEMENT_KSESSION, new KieSessionElementParser());
        droolsElementParserMap.put(ELEMENT_KSESSION_REF, new KieSessionElementParser());
        droolsElementParserMap.put(ELEMENT_EVENT_LISTENERS, new KieEventListenersElementParser());
        droolsElementParserMap.put(ELEMENT_KSTORE, new KieStoreElementParser());
        droolsElementParserMap.put(ELEMENT_ENVIRONMENT, new KieEnvironmentElementParser());
        droolsElementParserMap.put(ELEMENT_KCONTAINER, new KieContainerElementParser());
        droolsElementParserMap.put(ELEMENT_KRUNTIMEMANAGER, new KieRuntimeManagerElementParser());
        droolsElementParserMap.put(ELEMENT_KSESSION_RUNTIMEMANAGER, new KieRuntimeManagerSessionElementParser());
        droolsElementParserMap.put(ELEMENT_KMODULE, new KieModuleElementParser());
    }

    public KieNamespaceHandler() {
    }

    public KieNamespaceHandler(List<URL> resources) {
        this.resources = resources;
    }

    @Override
    public URL getSchemaLocation(String namespace) {
        if(NS_URI.equals(namespace)) {
            return getClass().getResource("kie-aries-blueprint.xsd");
        } else {
            return null;
        }
    }

    @Override
    public Set<Class> getManagedClasses() {
        return null;
    }

    @Override
    public Metadata parse(Element element, ParserContext parserContext) {
        String elementName = element.getLocalName();
        AbstractElementParser elementParser = droolsElementParserMap.get(elementName);
        if ( elementParser == null) {
            throw new ComponentDefinitionException("Unsupported Kie Blueprint Element '"+elementName+"'");
        }
        if (ELEMENT_KMODULE.equalsIgnoreCase(elementName)) {
            addKieObjectsProcessor(element, parserContext, elementParser);
        }
        return elementParser.parseElement(parserContext, element);
    }

    private void addKieObjectsProcessor(Element element, ParserContext context, AbstractElementParser elementParser) {
        // Register processors
        MutablePassThroughMetadata beanProcessorFactory = context.createMetadata(MutablePassThroughMetadata.class);

        String id = elementParser.getId(context, element);
        String contextId = ".kie.processor."+ id;
        beanProcessorFactory.setId(contextId);
        KieObjectsInjector kieObjectsInjector = new KieObjectsInjector(id, context);
        beanProcessorFactory.setObject(new PassThroughCallable<Object>(kieObjectsInjector));

        MutableBeanMetadata beanProcessor = context.createMetadata(MutableBeanMetadata.class);
        beanProcessor.setId(".droolsBlueprint.processor.bean." + id);
        beanProcessor.setRuntimeClass(KieObjectsInjector.class);
        beanProcessor.setFactoryComponent(beanProcessorFactory);
        beanProcessor.setFactoryMethod("call");
        beanProcessor.setProcessor(true);
        beanProcessor.setInitMethod("afterPropertiesSet");
        beanProcessor.addProperty("blueprintContainer", AbstractElementParser.createRef(context, "blueprintContainer"));
        context.getComponentDefinitionRegistry().registerComponentDefinition(beanProcessor);
    }

    @Override
    public ComponentMetadata decorate(Node node, ComponentMetadata componentMetadata, ParserContext parserContext) {
        System.out.println("decorate :: "+ node.getNodeName());
        return null;
    }


    public static class PassThroughCallable<T> implements Callable<T> {

        private T value;

        public PassThroughCallable(T value) {
            this.value = value;
        }

        public T call() throws Exception {
            return value;
        }
    }
}
