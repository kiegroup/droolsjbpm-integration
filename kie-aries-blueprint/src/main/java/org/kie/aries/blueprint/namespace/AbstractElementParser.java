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
import org.apache.aries.blueprint.mutable.MutableComponentMetadata;
import org.apache.aries.blueprint.mutable.MutableRefMetadata;
import org.apache.aries.blueprint.mutable.MutableValueMetadata;
import org.osgi.service.blueprint.reflect.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class AbstractElementParser {

    public static final String ID_ATTRIBUTE = "id";

    public String getAttributeValue(Node node, String attributeName) {
        if (node.getAttributes().getNamedItem(attributeName) != null) {
            return node.getAttributes().getNamedItem(attributeName).getNodeValue();
        } else {
            return null;
        }
    }

    public String getId(ParserContext context, Element element) {
        if (element.hasAttribute(ID_ATTRIBUTE)) {
            return element.getAttribute(ID_ATTRIBUTE);
        } else {
            return generateId(context);
        }
    }

    public String getId(ParserContext context, Node element) {
        if (element.getAttributes().getNamedItem(ID_ATTRIBUTE) != null) {
            return element.getAttributes().getNamedItem(ID_ATTRIBUTE).getNodeValue();
        } else {
            return generateId(context);
        }
    }

    public void generateIdIfNeeded(ParserContext context, MutableComponentMetadata metadata) {
        if (metadata.getId() == null) {
            metadata.setId(generateId(context));
        }
    }

    protected NullMetadata createNullMetadata() {
        return new NullMetadata() {};
    }

    private String generateId(ParserContext context) {
        String id;
        do {
            id = ".drools-" + ++idCounter;
        } while (context.getComponentDefinitionRegistry().containsComponentDefinition(id));
        return id;
    }
    private int idCounter;

    public int getIdCounter() {
        return idCounter;
    }

    public void setIdCounter(int idCounter) {
        this.idCounter = idCounter;
    }

    public static ValueMetadata createValue(ParserContext context, String value) {
        return createValue(context, value, null);
    }

    public static ValueMetadata createValue(ParserContext context, String value, String type) {
        MutableValueMetadata m = context.createMetadata(MutableValueMetadata.class);
        m.setStringValue(value);
        m.setType(type);
        return m;
    }

    public static ValueMetadata createValue(ParserContext context, int value) {
        return createValue(context, value, "java.lang.Integer");
    }

    public static ValueMetadata createValue(ParserContext context, int value, String type) {
        MutableValueMetadata m = context.createMetadata(MutableValueMetadata.class);
        m.setStringValue(""+value);
        m.setType(type);
        return m;
    }

    public static MutableRefMetadata createRef(ParserContext context, String value) {
        MutableRefMetadata m = context.createMetadata(MutableRefMetadata.class);
        m.setComponentId(value);
        return m;
    }

    public abstract Metadata parseElement(ParserContext context, Element element);

    /**
     * Adds 'bundleContext' property into the specific bean metadata. Bundle context can then be used to get
     * a bundle classloader which is needed in order to correctly use resources from other bundles (e.g. domain classes
     * in different bundle from the ones with DRL rules)
     *
     * @param beanMetadata mutable bean metadata holding
     * @param context blueprint parser context
     */
    protected void addBundleContextProperty(MutableBeanMetadata beanMetadata, ParserContext context) {
        beanMetadata.addProperty("bundleContext", createRef(context, "blueprintBundleContext"));
    }
}
