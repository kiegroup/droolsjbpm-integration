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

public class KieRuntimeManagerSessionElementParser extends AbstractElementParser {

    public static final String ATTRIBUTE_KRUNTIME_MANAGER_REF = "kruntimeManager-ref";

    @Override
    public ComponentMetadata parseElement(ParserContext context, Element element) {

        String id = getId(context, element);
        String ref = element.getAttribute(ATTRIBUTE_KRUNTIME_MANAGER_REF);

        MutableBeanMetadata beanMetadata = (MutableBeanMetadata) context.createMetadata(BeanMetadata.class);
        beanMetadata.setClassName("org.kie.aries.blueprint.factorybeans.KieRuntimeManagerFactoryBean");
        beanMetadata.setFactoryMethod("createSession");
        beanMetadata.setId(id);

        if (!StringUtils.isEmpty(ref)) {
            beanMetadata.addArgument(createRef(context, ref), null, 0);
        } else {
            throw new ComponentDefinitionException("Mandatory attribute 'kruntimeManager-ref' missing for ksession. Cannot continue.");
        }

        beanMetadata.setActivation(ComponentMetadata.ACTIVATION_LAZY);
        return beanMetadata;
    }

}
