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
import org.apache.aries.blueprint.reflect.*;
import org.kie.aries.blueprint.factorybeans.KieListenerAdaptor;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.WorkingMemoryEventListener;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class KieEventListenersElementParser extends AbstractElementParser {
    @Override
    public ComponentMetadata parseElement(ParserContext context, Element element) {
        String id = getId(context, element);
        BeanMetadataImpl componentMetadata = getBeanMetadata(context, element);
        componentMetadata.setId(id);
        return componentMetadata;
    }

    protected static BeanMetadataImpl getBeanMetadata(ParserContext context, Element element) {
        NodeList nodeList = element.getChildNodes();
        CollectionMetadataImpl collectionMetadata = context.createMetadata(CollectionMetadataImpl.class);
        collectionMetadata.setCollectionClass(ArrayList.class);
        for (int i=0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            String localName = node.getLocalName();
            if ( localName == null) {
                continue;
            }
            BeanMetadataImpl beanMetadata = context.createMetadata(BeanMetadataImpl.class);
            beanMetadata.setClassName(KieListenerAdaptor.class.getName());
            beanMetadata.setActivation(ComponentMetadata.ACTIVATION_LAZY);

            Node refValue = node.getAttributes().getNamedItem("ref");
            String type="";
            if ("workingMemoryEventListener".equalsIgnoreCase(localName) ){
                type = WorkingMemoryEventListener.class.getName();
            } else  if ("processEventListener".equalsIgnoreCase(localName)){
                type = ProcessEventListener.class.getName();
            } else if ("agendaEventListener".equalsIgnoreCase(localName)){
                type = AgendaEventListener.class.getName();
            }

            BeanArgumentImpl argument = new BeanArgumentImpl();
            argument.setIndex(0);
            argument.setValue(createValue(context, type));
            beanMetadata.addArgument(argument);

            argument = new BeanArgumentImpl();
            argument.setIndex(1);
            argument.setValue(createRef(context, refValue.getTextContent()));
            beanMetadata.addArgument(argument);

            collectionMetadata.addValue(beanMetadata);
        }

        BeanMetadataImpl componentMetadata = context.createMetadata(BeanMetadataImpl.class);
        componentMetadata.setClassName("java.util.ArrayList");
        BeanArgumentImpl argument = new BeanArgumentImpl();
        argument.setIndex(0);
        argument.setValue(collectionMetadata);
        componentMetadata.addArgument(argument);
        return componentMetadata;
    }
}
