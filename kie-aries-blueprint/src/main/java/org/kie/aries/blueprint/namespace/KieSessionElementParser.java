/*
 * Copyright 2013 JBoss Inc
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
package org.kie.aries.blueprint.namespace;

import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.apache.aries.blueprint.mutable.MutableCollectionMetadata;
import org.drools.core.util.StringUtils;
import org.osgi.service.blueprint.container.ComponentDefinitionException;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KieSessionElementParser extends AbstractElementParser {

    public static final String ATTRIBUTE_TYPE = "type";
    public static final String ATTRIBUTE_KBASE_REF = "kbase-ref";
    public static final String ATTRIBUTE_LISTENERS_REF = "listeners-ref";
    public static final String ATTRIBUTE_RELEASEID = "releaseId";

    public static final String ELEMENT_NAME_KSESSION_REF = "ksession-ref";
    public static final String ELEMENT_NAME_KSESSION = "ksession";

    @Override
    public ComponentMetadata parseElement(ParserContext context, Element element) {
        String id = getId(context, element);

        String type = element.getAttribute(ATTRIBUTE_TYPE);
        String listenersRef = element.getAttribute(ATTRIBUTE_LISTENERS_REF);
        String kbaseRef = element.getAttribute(ATTRIBUTE_KBASE_REF);
        String releaseId = element.getAttribute(ATTRIBUTE_RELEASEID);

        String localName = element.getLocalName();

        if (StringUtils.isEmpty(id)) {
            throw new ComponentDefinitionException("Mandatory attribute 'id' missing for "+localName+". Cannot continue.");
        }
        if ( ELEMENT_NAME_KSESSION.equalsIgnoreCase(localName)) {
            if (StringUtils.isEmpty(type)) {
                throw new ComponentDefinitionException("Mandatory attribute 'type' missing for ksession. Cannot continue.");
            }
            if (StringUtils.isEmpty(kbaseRef)) {
                throw new ComponentDefinitionException("Mandatory attribute 'kbase-ref' missing for ksession. Cannot continue.");
            }
        }

        MutableBeanMetadata beanMetadata = context.createMetadata(MutableBeanMetadata.class);
        beanMetadata.setActivation(ComponentMetadata.ACTIVATION_LAZY);
        beanMetadata.setId(id);
        beanMetadata.setClassName("org.kie.aries.blueprint.factorybeans.KieObjectsFactoryBean");

        //(String id, ReleaseId releaseId, List<KieListenerAdaptor> listeners, String kbaseRef, String type){
        /*BeanArgumentImpl argument = new BeanArgumentImpl();
        argument.setIndex(0);
        argument.setValue(createValue(context, id));
        beanMetadata.addArgument(argument);*/
        beanMetadata.addArgument(createValue(context, id), null, 0);


        //argument = new BeanArgumentImpl();
        //argument.setIndex(1);
        if (StringUtils.isEmpty(releaseId)) {
            //argument.setValue(createNullMetadata());
            beanMetadata.addArgument(createNullMetadata(), null, 1);
        } else {
            //argument.setValue(createRef(context, releaseId));
            beanMetadata.addArgument(createRef(context, releaseId), null, 1);
        }
        // beanMetadata.addArgument(argument);

        //argument = new BeanArgumentImpl();
        //argument.setIndex(2);
        if (!StringUtils.isEmpty(listenersRef)) {
            // argument.setValue(createRef(context, listenersRef));
            beanMetadata.addArgument(createRef(context, listenersRef), null, 2);
        }else{
            //check if there are any child listener nodes
            Metadata metadata = checkForChildListeners(context, element);
            // argument.setValue(metadata);
            beanMetadata.addArgument(metadata, null, 2);
        }
        // beanMetadata.addArgument(argument);

        MutableCollectionMetadata collectionMetadata = KieSessionLoggerElementParser.parseConsoleLoggers(this, context, element);
        //argument = new BeanArgumentImpl();
        //argument.setIndex(3);
        //argument.setValue(collectionMetadata);
        //beanMetadata.addArgument(argument);
        beanMetadata.addArgument(collectionMetadata, null, 3);

        collectionMetadata = KieSessionBatchElementParser.parseBatchElement(this, context, element);
        // argument = new BeanArgumentImpl();
        // argument.setIndex(4);
        // argument.setValue(collectionMetadata);
        // beanMetadata.addArgument(argument);
        beanMetadata.addArgument(collectionMetadata, null, 4);

        beanMetadata.setActivation(ComponentMetadata.ACTIVATION_LAZY);

        if ( ELEMENT_NAME_KSESSION.equalsIgnoreCase(localName)) {
            /*argument = new BeanArgumentImpl();
            argument.setIndex(5);
            argument.setValue(createValue(context, kbaseRef));
            beanMetadata.addArgument(argument);*/
            beanMetadata.addArgument(createValue(context, kbaseRef), null, 5);

            /*argument = new BeanArgumentImpl();
            argument.setIndex(6);
            argument.setValue(createValue(context, type));
            beanMetadata.addArgument(argument);*/
            beanMetadata.addArgument(createValue(context, type), null, 6);
            beanMetadata.setFactoryMethod("createKieSession");
        } else {
            beanMetadata.setFactoryMethod("createKieSessionRef");
        }

        return beanMetadata;
    }


    protected Metadata checkForChildListeners(ParserContext context, Element element){
        NodeList nodeList = element.getChildNodes();

        for (int i=0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            String localName = node.getLocalName();
            if ( localName == null) {
                continue;
            }
            if ("workingMemoryEventListener".equalsIgnoreCase(localName) || "processEventListener".equalsIgnoreCase(localName) || "agendaEventListener".equalsIgnoreCase(localName)){
                // run the loop only if we have atleast one child
                // the KieEventListenersElementParser.getBeanMetadata method will loop and pick up all the listeners
                MutableBeanMetadata beanMetadata = KieEventListenersElementParser.getBeanMetadata(context, element);
                return beanMetadata;
            }
        }
        return createNullMetadata();
    }


}
