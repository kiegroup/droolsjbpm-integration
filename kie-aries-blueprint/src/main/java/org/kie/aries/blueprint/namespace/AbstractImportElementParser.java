/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.w3c.dom.Element;

public abstract class AbstractImportElementParser extends AbstractElementParser {

    @Override
    public ComponentMetadata parseElement( ParserContext context, Element element ) {
        String name = element.getAttribute("name");

        MutableBeanMetadata beanMetadata = (MutableBeanMetadata) context.createMetadata( BeanMetadata.class );
        beanMetadata.setClassName("org.kie.aries.blueprint.factorybeans.KieObjectsFactoryBean");
        beanMetadata.setFactoryMethod(getFactoryMethodName());
        beanMetadata.setId(name);
        beanMetadata.addArgument(createValue(context, name),null,0);

        addBundleContextProperty(beanMetadata, context);
        beanMetadata.setActivation(ComponentMetadata.ACTIVATION_LAZY);

        return beanMetadata;
    }

    protected abstract String getFactoryMethodName();
}
