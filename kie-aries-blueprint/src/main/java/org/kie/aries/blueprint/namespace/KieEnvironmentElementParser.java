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
import org.apache.aries.blueprint.mutable.MutableMapMetadata;
import org.apache.aries.blueprint.mutable.MutableRefMetadata;
import org.drools.core.marshalling.impl.IdentityPlaceholderResolverStrategy;
import org.drools.core.marshalling.impl.SerializablePlaceholderResolverStrategy;
import org.drools.core.util.StringUtils;
import org.jbpm.marshalling.impl.ProcessInstanceResolverStrategy;
import org.kie.api.runtime.EnvironmentName;
import org.kie.aries.blueprint.factorybeans.KieObjectsFactoryBean;
import org.kie.aries.blueprint.helpers.JPAPlaceholderResolverStrategyHelper;
import org.osgi.service.blueprint.container.ComponentDefinitionException;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class KieEnvironmentElementParser extends AbstractElementParser {

    public static final String ATTRIBUTE_ID = "id";
    public static final String ATTRIBUTE_REF = "ref";
    public static final String ATTRIBUTE_SCOPE = "scope";
    public static final String ATTRIBUTE_STRATEGY_ACCEPTOR_REF = "strategy-acceptor-ref";
    public static final String ATTRIBUTE_ENV_REF = "env-ref";

    public static final String ELEMENT_ENTITY_MANAGER_FACTORY = "entity-manager-factory";
    public static final String ELEMENT_TRANSACTION_MANAGER = "transaction-manager";
    public static final String ELEMENT_USER_TRANSACTION = "user-transaction";
    public static final String ELEMENT_TRANSACTION_SYNC_REGISTRY = "transaction-sync-registry";
    public static final String ELEMENT_GLOBALS = "globals";
    public static final String ELEMENT_DATE_FORMATS = "date-formats";
    public static final String ELEMENT_CALENDARS = "calendars";
    public static final String ELEMENT_OBJECT_MARSHALLING_STRATEGIES = "object-marshalling-strategies";
    public static final String ELEMENT_SERIALIZABLE_PLACEHOLDER_RESOLVER_STRATEGY = "serializable-placeholder-resolver-strategy";
    public static final String ELEMENT_IDENTITY_PLACEHOLDER_RESOLVER_STRATEGY = "identity-placeholder-resolver-strategy";
    public static final String ELEMENT_PROCESS_INSTANCE_RESOLVER_STRATEGY = "process-instance-resolver-strategy";
    public static final String ELEMENT_JPA_PLACEHOLDER_RESOLVER_STRATEGY = "jpa-placeholder-resolver-strategy";
    public static final String ELEMENT_SCOPED_ENTITY_MANAGER = "scoped-entity-manager";
    public static final String ELEMENT_CUSTOM_MARSHALLING_STRATEGY = "custom-marshalling-strategy";
    public static final String ELEMENT_BEAN = "bean";
    public static final String ELEMENT_STRATEGY_ACCEPTOR = "strategy-acceptor";

    public static final String PROPERTY_ENTITY_MANAGER_FACTORY = "entityManagerFactory";
    public static final String PROPERTY_TRANSACTION_MANAGER = "transactionManager";
    public static final String PROPERTY_USER_TRANSACTION = "userTransaction";
    public static final String PROPERTY_TRANSACTION_SYNC_REGISTRY = "transactionSyncRegistry";
    public static final String PROPERTY_GLOBALS = EnvironmentName.GLOBALS;
    public static final String PROPERTY_CALENDARS = "calendars";
    public static final String PROPERTY_DATE_FORMATS = "dateFormats";
    public static final String PROPERTY_JPA_PLACE_HOLDER_RESOLVER_STRATEGY_ENV = "jpaPlaceHolderResolverStrategyEnv";
    public static final String PROPERTY_APP_SCOPED_ENTITY_MANAGER = "appScopedEntityManager";
    public static final String PROPERTY_CMD_SCOPED_ENTITY_MANAGER = "cmdScopedEntityManager";
    public static final String PROPERTY_SERIALIZABLE_PLACEHOLDER_RESOLVER_STRATEGY_ACCEPTOR = "serializablePlaceholderResolverStrategyAcceptor";
    public static final String PROPERTY_IDENTITY_PLACEHOLDER_RESOLVER_STRATEGY_ACCEPTOR = "identityPlaceholderResolverStrategyAcceptor";
    public static final String PROPERTY_CUSTOM_MARSHALLING_STRATEGY = "customMarshallingStrategies";
    public static final String PROPERTY_OBJECT_MARSHALLING_ORDER = "objectMarshallersOrder";

    public static final String PROPERTY_NAME = "name";

    @Override
    public ComponentMetadata parseElement(ParserContext context, Element element) {
        String id = getId(context, element);
        if (StringUtils.isEmpty(id)){
            throw new ComponentDefinitionException("'id' attribute is missing for environment definition.");
        }

        MutableMapMetadata envParamMetadata = context.createMetadata(MutableMapMetadata.class);
        envParamMetadata.setKeyType(String.class.getName());
        envParamMetadata.setValueType(Object.class.getName());

        checkAndSetReference(context, element, envParamMetadata, ELEMENT_ENTITY_MANAGER_FACTORY, EnvironmentName.ENTITY_MANAGER_FACTORY, ATTRIBUTE_REF);
        checkAndSetReference(context, element, envParamMetadata, ELEMENT_TRANSACTION_MANAGER, EnvironmentName.TRANSACTION_MANAGER, ATTRIBUTE_REF);
        checkAndSetReference(context, element, envParamMetadata, ELEMENT_TRANSACTION_SYNC_REGISTRY, EnvironmentName.TRANSACTION_SYNCHRONIZATION_REGISTRY, ATTRIBUTE_REF);
        checkAndSetReference(context, element, envParamMetadata, ELEMENT_USER_TRANSACTION, EnvironmentName.TRANSACTION, ATTRIBUTE_REF);
        checkAndSetReference(context, element, envParamMetadata, ELEMENT_GLOBALS, EnvironmentName.GLOBALS, ATTRIBUTE_REF);
        checkAndSetReference(context, element, envParamMetadata, ELEMENT_CALENDARS, EnvironmentName.CALENDARS, ATTRIBUTE_REF);

        MutableCollectionMetadata strategiesCollectionMetadata = context.createMetadata(MutableCollectionMetadata.class);
        strategiesCollectionMetadata.setCollectionClass(ArrayList.class);
        NodeList nodeList = element.getElementsByTagName(element.getPrefix()+":"+ELEMENT_OBJECT_MARSHALLING_STRATEGIES);
        if (nodeList != null && nodeList.getLength() > 0){
            Node node = nodeList.item(0);
            if ( node instanceof Element ){
                Element strategiesElement = (Element)node;
                NodeList strategiesChildNodes = strategiesElement.getChildNodes();
                for ( int i=0; i < strategiesChildNodes.getLength(); i++){
                    Node childNode = strategiesChildNodes.item(i);
                    if ( childNode instanceof  Element) {
                        Element child = (Element) childNode;
                        String localName = child.getLocalName();

                        if (ELEMENT_SERIALIZABLE_PLACEHOLDER_RESOLVER_STRATEGY.equalsIgnoreCase(localName)) {
                            String ref = child.getAttribute(ATTRIBUTE_STRATEGY_ACCEPTOR_REF);
                            MutableBeanMetadata beanMetadata = context.createMetadata(MutableBeanMetadata.class);
                            beanMetadata.setClassName(SerializablePlaceholderResolverStrategy.class.getName());
                            beanMetadata.setActivation(MutableBeanMetadata.ACTIVATION_LAZY);

                            // BeanArgumentImpl argument = new BeanArgumentImpl();
                            // argument.setIndex(0);
                            if (!StringUtils.isEmpty(ref)) {
                                //argument.setValue(createRef(context, ref));
                                beanMetadata.addArgument(createRef(context, ref), null, 0);

                            } else {
                                MutableBeanMetadata defaultStrategyAcceptor = context.createMetadata(MutableBeanMetadata.class);
                                defaultStrategyAcceptor.setClassName(KieObjectsFactoryBean.class.getName());
                                defaultStrategyAcceptor.setFactoryMethod("createDefaultAcceptor");
                                beanMetadata.addArgument(defaultStrategyAcceptor, null, 0);

                                //argument.setValue(defaultStrategyAcceptor);
                            }
                            //beanMetadata.addArgument(argument);
                            strategiesCollectionMetadata.addValue(beanMetadata);
                        } else if (ELEMENT_IDENTITY_PLACEHOLDER_RESOLVER_STRATEGY.equalsIgnoreCase(localName)) {
                            String ref = child.getAttribute(ATTRIBUTE_STRATEGY_ACCEPTOR_REF);
                            MutableBeanMetadata beanMetadata = context.createMetadata(MutableBeanMetadata.class);
                            beanMetadata.setClassName(IdentityPlaceholderResolverStrategy.class.getName());
                            beanMetadata.setActivation(MutableBeanMetadata.ACTIVATION_LAZY);

                            //BeanArgumentImpl argument = new BeanArgumentImpl();
                            //argument.setIndex(0);
                            if (!StringUtils.isEmpty(ref)) {
                                //argument.setValue(createRef(context, ref));
                                beanMetadata.addArgument(createRef(context, ref), null, 0);

                            } else {
                                MutableBeanMetadata defaultStrategyAcceptor = context.createMetadata(MutableBeanMetadata.class);
                                defaultStrategyAcceptor.setClassName(KieObjectsFactoryBean.class.getName());
                                defaultStrategyAcceptor.setFactoryMethod("createDefaultAcceptor");
                                // argument.setValue(defaultStrategyAcceptor);
                                beanMetadata.addArgument(defaultStrategyAcceptor, null, 0);
                            }
                            //beanMetadata.addArgument(argument);
                            strategiesCollectionMetadata.addValue(beanMetadata);
                        } else if (ELEMENT_PROCESS_INSTANCE_RESOLVER_STRATEGY.equalsIgnoreCase(localName)) {
                            MutableBeanMetadata beanMetadata = context.createMetadata(MutableBeanMetadata.class);
                            beanMetadata.setClassName(ProcessInstanceResolverStrategy.class.getName());
                            beanMetadata.setActivation(MutableBeanMetadata.ACTIVATION_LAZY);
                            strategiesCollectionMetadata.addValue(beanMetadata);
                        } else if (ELEMENT_JPA_PLACEHOLDER_RESOLVER_STRATEGY.equalsIgnoreCase(localName)) {
                            MutableBeanMetadata beanMetadata = context.createMetadata(MutableBeanMetadata.class);
                            /*
                            JPAPlaceholderResolverStrategyHelper is required to be used because the original
                            JPAPlaceholderResolverStrategy takes a reference to a environment, and if it refers to the same environemnt
                            it causes a circular dependency.
                             */
                            beanMetadata.setClassName(JPAPlaceholderResolverStrategyHelper.class.getName());
                            beanMetadata.setActivation(MutableBeanMetadata.ACTIVATION_LAZY);
                            String ref = child.getAttribute(ATTRIBUTE_REF);
                            if (!StringUtils.isEmpty(ref)) {
                                /*BeanArgumentImpl argument = new BeanArgumentImpl();
                                argument.setIndex(0);
                                argument.setValue(createRef(context, id));
                                beanMetadata.addArgument(argument);*/
                                beanMetadata.addArgument(createRef(context, id), null, 0);
                            }
                            strategiesCollectionMetadata.addValue(beanMetadata);
                        } else if (ELEMENT_CUSTOM_MARSHALLING_STRATEGY.equalsIgnoreCase(localName)) {
                            String ref = child.getAttribute(ATTRIBUTE_REF);
                            if (!StringUtils.isEmpty(ref)) {
                                strategiesCollectionMetadata.addValue(createRef(context, ref));
                            } else {
                                throw new ComponentDefinitionException("'ref' attribute is missing for custom marshaller definition.");
                            }
                        }
                    }
                }
            }
        }
        MutableBeanMetadata beanMetadata = context.createMetadata(MutableBeanMetadata.class);
        beanMetadata.setActivation(ComponentMetadata.ACTIVATION_LAZY);
        beanMetadata.setId(id);
        beanMetadata.setClassName("org.kie.aries.blueprint.factorybeans.KieObjectsFactoryBean");

        /*BeanArgumentImpl argument = new BeanArgumentImpl();
        argument.setIndex(0);
        argument.setValue(createValue(context, id));
        beanMetadata.addArgument(argument);*/
        beanMetadata.addArgument(createValue(context, id), null, 0);

        /*argument = new BeanArgumentImpl();
        argument.setIndex(1);
        argument.setValue(envParamMetadata);
        beanMetadata.addArgument(argument); */
        beanMetadata.addArgument(envParamMetadata, null, 1);

        /*argument = new BeanArgumentImpl();
        argument.setIndex(2);
        argument.setValue(strategiesCollectionMetadata);
        beanMetadata.addArgument(argument);*/
        beanMetadata.addArgument(strategiesCollectionMetadata, null, 2);

        beanMetadata.setFactoryMethod("createEnvironment");
        return beanMetadata;
    }

    protected void checkAndSetReference(ParserContext context, Element envElement, MutableMapMetadata envParamMetadata, String elementTag, String propName, String refAttribute) {

        String prefix = envElement.getPrefix();
        NodeList nodeList = envElement.getElementsByTagName(prefix+":"+elementTag);
        if ( nodeList != null && nodeList.getLength() > 0){
            Node childNode = nodeList.item(0);
            if ( childNode instanceof  Element) {
                Element childElement = (Element)childNode;
                String ref = childElement.getAttribute(refAttribute);
                if (!StringUtils.isEmpty(ref)){
                   // envParamMetadata.
                    MutableRefMetadata refData = context.createMetadata(MutableRefMetadata.class);
                    refData.setComponentId(ref);
                    // createRef(context, ref);
                    // ((RefMetadataImpl)refData).setComponentId(propName);
                    envParamMetadata.addEntry(createValue(context, propName), refData);
                }
            }
        }
    }
}
