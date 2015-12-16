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
import org.drools.core.util.StringUtils;
import org.osgi.service.blueprint.container.ComponentDefinitionException;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.w3c.dom.Element;

public class KieRuntimeManagerElementParser extends AbstractElementParser {


    @Override
    public ComponentMetadata parseElement(ParserContext context, Element element) {

        String id = getId(context, element);
        String type = element.getAttribute("type");
        String asset = element.getAttribute("asset");
        String assetType = element.getAttribute("assetType");

        MutableBeanMetadata beanMetadata = (MutableBeanMetadata) context.createMetadata(BeanMetadata.class);
        beanMetadata.setClassName("org.kie.aries.blueprint.factorybeans.KieRuntimeManagerFactoryBean");
        beanMetadata.setFactoryMethod("createRuntime");
        beanMetadata.setId(id);

        if (!StringUtils.isEmpty(type)) {
            beanMetadata.addArgument(createValue(context, type), null, 0);
        } else {
            beanMetadata.addArgument(createValue(context, "empty"), null, 0);
        }

        if (!StringUtils.isEmpty(asset)) {
            beanMetadata.addArgument(createValue(context, asset), null, 1);
        } else {
            throw new ComponentDefinitionException("'asset' attribute is missing for custom marshaller definition.");
        }

        if (!StringUtils.isEmpty(assetType)) {
            beanMetadata.addArgument(createValue(context, assetType), null, 2);
        } else {
            beanMetadata.addArgument(createValue(context, "BPMN2"), null, 2);
        }

        beanMetadata.setActivation(ComponentMetadata.ACTIVATION_LAZY);
        return beanMetadata;
    }

}
