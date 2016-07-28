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
package org.kie.aries.blueprint.namespace;

import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.apache.aries.blueprint.mutable.MutableCollectionMetadata;
import org.apache.aries.blueprint.mutable.MutableRefMetadata;
import org.drools.core.util.StringUtils;
import org.kie.aries.blueprint.factorybeans.KieLoggerAdaptor;
import org.osgi.service.blueprint.container.ComponentDefinitionException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

import static org.kie.aries.blueprint.namespace.AbstractElementParser.createValue;

class KieSessionLoggerElementParser {

    public static final String LOGGER_ATTRIBUTE_FILE = "file";
    public static final String LOGGER_ATTRIBUTE_ID = "id";
    public static final String LOGGER_ATTRIBUTE_THREADED = "threaded";
    public static final String LOGGER_ATTRIBUTE_LOGGER_TYPE = "loggerType";
    public static final String LOGGER_ATTRIBUTE_INTERVAL = "interval";

    public static MutableCollectionMetadata parseConsoleLoggers(KieSessionElementParser kieLoggerElementParser, ParserContext context, Element element){

        MutableCollectionMetadata collectionMetadata = context.createMetadata(MutableCollectionMetadata.class);
        collectionMetadata.setCollectionClass(ArrayList.class);
        String prefix = element.getPrefix();
        NodeList consoleLoggerList = element.getElementsByTagName(prefix+":consoleLogger");
        if ( consoleLoggerList != null && consoleLoggerList.getLength() > 0){
            for (int i=0; i < consoleLoggerList.getLength(); i++){
                Node loggerNode = consoleLoggerList.item(i);
                MutableBeanMetadata componentMetadata = context.createMetadata(MutableBeanMetadata.class);
                componentMetadata.setId(kieLoggerElementParser.getId(context, loggerNode));
                kieLoggerElementParser.generateIdIfNeeded(context, componentMetadata);
                componentMetadata.setClassName(KieLoggerAdaptor.class.getName());
                context.getComponentDefinitionRegistry().registerComponentDefinition(componentMetadata);

                MutableRefMetadata refMetadata = context.createMetadata(MutableRefMetadata.class);
                refMetadata.setComponentId(componentMetadata.getId());
                collectionMetadata.addValue(refMetadata);
            }
        }

        NodeList fileLoggerList = element.getElementsByTagName(prefix+":fileLogger");
        if ( fileLoggerList != null && fileLoggerList.getLength() > 0){
            for (int i=0; i < fileLoggerList.getLength(); i++){
                Node loggerNode = fileLoggerList.item(i);
                MutableBeanMetadata componentMetadata = context.createMetadata(MutableBeanMetadata.class);
                String id = kieLoggerElementParser.getId(context, loggerNode);
                if ( id != null) {
                    componentMetadata.setId(id);
                }
                kieLoggerElementParser.generateIdIfNeeded(context, componentMetadata);
                context.getComponentDefinitionRegistry().registerComponentDefinition(componentMetadata);

                componentMetadata.setClassName(KieLoggerAdaptor.class.getName());

                String fileName = kieLoggerElementParser.getAttributeValue(loggerNode, LOGGER_ATTRIBUTE_FILE);
                if (StringUtils.isEmpty(LOGGER_ATTRIBUTE_FILE)){
                    throw new ComponentDefinitionException(LOGGER_ATTRIBUTE_FILE+" attribute is missing for logger ("+ kieLoggerElementParser.getId(context, element)+")");
                }
                fileName =  ExpressionUtils.resolveExpressionInPath(fileName);
                componentMetadata.addProperty("file", createValue(context, fileName));

                String threaded = kieLoggerElementParser.getAttributeValue(loggerNode, LOGGER_ATTRIBUTE_THREADED);
                if (!StringUtils.isEmpty(LOGGER_ATTRIBUTE_FILE) && "true".equalsIgnoreCase(threaded)){
                    componentMetadata.addProperty("loggerType", createValue(context, KieLoggerAdaptor.KNOWLEDGE_LOGGER_TYPE.LOGGER_TYPE_THREADED_FILE.toString()));
                    String interval = kieLoggerElementParser.getAttributeValue(loggerNode, LOGGER_ATTRIBUTE_INTERVAL);
                    if ( !StringUtils.isEmpty(interval)){
                        try{
                            int nInterval = Integer.parseInt(interval);
                            componentMetadata.addProperty("interval", createValue(context, nInterval));
                        }catch (Exception e){
                            //should never happen, the XSD would prevent non-integers coming this far.
                        }
                    }
                    if (StringUtils.isEmpty(LOGGER_ATTRIBUTE_FILE)){
                        throw new ComponentDefinitionException(LOGGER_ATTRIBUTE_FILE+" attribute is missing for logger ("+ kieLoggerElementParser.getId(context, element)+")");
                    }

                } else{
                    componentMetadata.addProperty("loggerType", createValue(context, KieLoggerAdaptor.KNOWLEDGE_LOGGER_TYPE.LOGGER_TYPE_FILE.toString()));
                }

                MutableRefMetadata refMetadata = context.createMetadata(MutableRefMetadata.class);
                refMetadata.setComponentId(componentMetadata.getId());
                collectionMetadata.addValue(refMetadata);
            }
        }
        return collectionMetadata;
    }

}
