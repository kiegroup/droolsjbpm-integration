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
import org.apache.aries.blueprint.reflect.BeanArgumentImpl;
import org.apache.aries.blueprint.reflect.BeanMetadataImpl;
import org.apache.aries.blueprint.reflect.CollectionMetadataImpl;
import org.apache.aries.blueprint.reflect.RefMetadataImpl;
import org.drools.core.command.runtime.SetGlobalCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.FireUntilHaltCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.util.StringUtils;
import org.kie.aries.blueprint.factorybeans.KieLoggerAdaptor;
import org.osgi.service.blueprint.container.ComponentDefinitionException;
import org.osgi.service.blueprint.reflect.CollectionMetadata;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

class KieSessionBatchElementParser {

    public static final String LOGGER_ATTRIBUTE_FILE = "file";
    public static final String LOGGER_ATTRIBUTE_ID = "id";
    public static final String LOGGER_ATTRIBUTE_THREADED = "threaded";
    public static final String LOGGER_ATTRIBUTE_LOGGER_TYPE = "loggerType";
    public static final String LOGGER_ATTRIBUTE_INTERVAL = "interval";

    public static CollectionMetadata parseBatchElement(KieSessionElementParser kieSessionElementParser, ParserContext context, Element element){

        CollectionMetadataImpl collectionMetadata = context.createMetadata(CollectionMetadataImpl.class);
        collectionMetadata.setCollectionClass(ArrayList.class);
        String prefix = element.getPrefix();
        NodeList batchNodeList = element.getElementsByTagName(prefix+":batch");
        if ( batchNodeList != null && batchNodeList.getLength() > 0){
            //only one batch node allowed.
            Node batchNode = batchNodeList.item(0);

            for (int i=0; i < batchNode.getChildNodes().getLength(); i++){
                Node n = batchNode.getChildNodes().item(i);
                if (n instanceof Element) {
                    Element e = (Element) n;
                    BeanMetadataImpl componentMetadata = context.createMetadata(BeanMetadataImpl.class);
                    if ("insert-object".equals(e.getLocalName())) {
                        String ref = e.getAttribute("ref");
                        if (StringUtils.isEmpty(ref)){
                            throw new ComponentDefinitionException("'ref' attribute is missing for insert-object for bean definition ("+ kieSessionElementParser.getId(context, element)+")");
                        }
                        componentMetadata.setClassName(InsertObjectCommand.class.getName());
                        BeanArgumentImpl argument = new BeanArgumentImpl();
                        argument.setIndex(0);
                        argument.setValue(kieSessionElementParser.createRef(context, ref));
                        componentMetadata.addArgument(argument);
                    } else if ("set-global".equals(e.getLocalName())) {
                        String ref = e.getAttribute("ref");
                        if (StringUtils.isEmpty(ref)){
                            throw new ComponentDefinitionException("'ref' attribute is missing for set-global for bean definition ("+ kieSessionElementParser.getId(context, element)+")");
                        }
                        String identifier = e.getAttribute("identifier");
                        if (StringUtils.isEmpty(identifier)){
                            throw new ComponentDefinitionException("'identifier' attribute is missing for set-global for bean definition ("+ kieSessionElementParser.getId(context, element)+")");
                        }
                        componentMetadata.setClassName(SetGlobalCommand.class.getName());
                        BeanArgumentImpl argument = new BeanArgumentImpl();
                        argument.setIndex(0);
                        argument.setValue(kieSessionElementParser.createValue(context, identifier));
                        componentMetadata.addArgument(argument);

                        argument = new BeanArgumentImpl();
                        argument.setIndex(1);
                        argument.setValue(kieSessionElementParser.createRef(context, ref));
                        componentMetadata.addArgument(argument);

                    } else if ("fire-until-halt".equals(e.getLocalName())) {
                        componentMetadata.setClassName(FireUntilHaltCommand.class.getName());
                    } else if ("fire-all-rules".equals(e.getLocalName())) {
                        componentMetadata.setClassName(FireAllRulesCommand.class.getName());
                        String max = e.getAttribute("max");
                        if (!StringUtils.isEmpty(max)) {
                            try {
                                BeanArgumentImpl argument = new BeanArgumentImpl();
                                argument.setIndex(0);
                                argument.setValue(kieSessionElementParser.createValue(context, Integer.parseInt(max)));
                                componentMetadata.addArgument(argument);
                            }catch (NumberFormatException e1){
                                //xsd will prevent this from happening.
                            }
                        }

                    } else if ("start-process".equals(e.getLocalName())) {
//                        String processId = e.getAttribute("process-id");
//                        if (StringUtils.isEmpty(processId)) {
//                            throw new ComponentDefinitionException("start-process must specify a process-id for bean definition ("+ kieSessionElementParser.getId(context, element)+")");
//                        }
//                        BeanArgumentImpl argument = new BeanArgumentImpl();
//                        try{
//                            argument.setValue(kieSessionElementParser.createValue(context, Integer.parseInt(processId)));
//                        }catch (NumberFormatException e1){
//                            //xsd will prevent this from happening.
//                        }
//                        componentMetadata.addArgument(argument);
//
//                        List<Element> params = DomUtils.getChildElementsByTagName(e, "parameter");
                    } else if ("signal-event".equals(e.getLocalName())) {
                        componentMetadata.setClassName(SignalEventCommand.class.getName());
                        String processInstanceId = e.getAttribute("process-instance-id");
                        BeanArgumentImpl argument = null;
                        int index = 0;
                        if (!StringUtils.isEmpty(processInstanceId)) {
                            argument = new BeanArgumentImpl();
                            try{
                                argument.setValue(kieSessionElementParser.createValue(context, Integer.parseInt(processInstanceId)));
                            }catch (NumberFormatException e1){
                                //xsd will prevent this from happening.
                            }
                            componentMetadata.addArgument(argument);
                            index++;
                        }
                        String ref = e.getAttribute("ref");
                        if (StringUtils.isEmpty(ref)){
                            throw new ComponentDefinitionException("'ref' attribute is missing for signal-event for bean definition ("+ kieSessionElementParser.getId(context, element)+")");
                        }
                        String eventType = e.getAttribute("event-type");
                        if (StringUtils.isEmpty(eventType)){
                            throw new ComponentDefinitionException("'event-type' attribute is missing for signal-event for bean definition ("+ kieSessionElementParser.getId(context, element)+")");
                        }
                        argument = new BeanArgumentImpl();
                        argument.setIndex(index++);
                        argument.setValue(kieSessionElementParser.createRef(context, ref));
                        componentMetadata.addArgument(argument);

                        argument = new BeanArgumentImpl();
                        argument.setIndex(index++);
                        argument.setValue(kieSessionElementParser.createValue(context, eventType));
                        componentMetadata.addArgument(argument);
                    } else {
                        throw new ComponentDefinitionException("Unknown child element found in batch element.");
                    }
                    collectionMetadata.addValue(componentMetadata);
                }
            }
        }
        return collectionMetadata;
    }

    private static Element getFirstElement(NodeList list) {
        for (int j = 0, lengthj = list.getLength(); j < lengthj; j++) {
            if (list.item(j) instanceof Element) {
                return (Element) list.item(j);
            }
        }
        return null;
    }
}
