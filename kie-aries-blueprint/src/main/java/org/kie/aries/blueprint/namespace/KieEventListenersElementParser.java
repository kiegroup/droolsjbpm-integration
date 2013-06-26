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

import java.util.ArrayList;

import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.apache.aries.blueprint.mutable.MutableCollectionMetadata;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.WorkingMemoryEventListener;
import org.kie.aries.blueprint.factorybeans.KieListenerAdaptor;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KieEventListenersElementParser extends AbstractElementParser {
    @Override
    public ComponentMetadata parseElement(ParserContext context, Element element) {
        String id = getId(context, element);
        MutableBeanMetadata componentMetadata = getBeanMetadata(context, element);
        componentMetadata.setId(id);
        return componentMetadata;
    }

    protected static MutableBeanMetadata getBeanMetadata(ParserContext context, Element element) {
        NodeList nodeList = element.getChildNodes();
        MutableCollectionMetadata collectionMetadata = context.createMetadata(MutableCollectionMetadata.class);
        collectionMetadata.setCollectionClass(ArrayList.class);
        for (int i=0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            String localName = node.getLocalName();
            if ( localName == null) {
                continue;
            }
            MutableBeanMetadata beanMetadata = context.createMetadata(MutableBeanMetadata.class);
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

            /*BeanArgumentImpl argument = new BeanArgumentImpl();
            argument.setIndex(0);
            argument.setValue(createValue(context, type));
            beanMetadata.addArgument(argument);*/
            beanMetadata.addArgument(createValue(context, type), null, 0);

            /*argument = new BeanArgumentImpl();
            argument.setIndex(1);
            argument.setValue(createRef(context, refValue.getTextContent()));
            beanMetadata.addArgument(argument);*/
            beanMetadata.addArgument(createRef(context, refValue.getTextContent()), null, 1);


            collectionMetadata.addValue(beanMetadata);
        }

        MutableBeanMetadata componentMetadata = context.createMetadata(MutableBeanMetadata.class);
        componentMetadata.setClassName("java.util.ArrayList");
        /*BeanArgumentImpl argument = new BeanArgumentImpl();
        argument.setIndex(0);
        argument.setValue(collectionMetadata);
        componentMetadata.addArgument(argument); */
        componentMetadata.addArgument(collectionMetadata, null, 0);

        return componentMetadata;
    }
}
