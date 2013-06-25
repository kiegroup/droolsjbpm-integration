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
import org.drools.core.util.StringUtils;
import org.osgi.service.blueprint.container.ComponentDefinitionException;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.w3c.dom.Element;

public class ReleaseIdElementParser extends AbstractElementParser {

    @Override
    public ComponentMetadata parseElement(ParserContext context, Element element) {
        String id = getId(context, element);

        String groupId = element.getAttribute("groupId");
        if (StringUtils.isEmpty(groupId)){
            throw new ComponentDefinitionException("groupId attribute is missing for releaseId ("+id+")");
        }
        String artifactId = element.getAttribute("artifactId");
        if (StringUtils.isEmpty(artifactId)){
            throw new ComponentDefinitionException("artifactId attribute is missing for releaseId ("+id+")");
        }
        String version = element.getAttribute("version");
        if (StringUtils.isEmpty(version)){
            throw new ComponentDefinitionException("version attribute is missing for releaseId ("+id+")");
        }

        MutableBeanMetadata beanMetadata = (MutableBeanMetadata) context.createMetadata(BeanMetadata.class);
        beanMetadata.setClassName("org.kie.aries.blueprint.factorybeans.KieObjectsFactoryBean");
        beanMetadata.setFactoryMethod("createReleaseId");
        beanMetadata.setId(id);

        /*BeanArgumentImpl argument = new BeanArgumentImpl();
        argument.setIndex(0);
        argument.setValue(createValue(context, id));
        beanMetadata.addArgument(argument);*/
        beanMetadata.addArgument(createValue(context, id), null, 0);

        /*argument = new BeanArgumentImpl();
        argument.setIndex(1);
        argument.setValue(createValue(context, groupId));
        beanMetadata.addArgument(argument);*/
        beanMetadata.addArgument(createValue(context, groupId), null, 1);

        /*argument = new BeanArgumentImpl();
        argument.setIndex(2);
        argument.setValue(createValue(context, artifactId));
        beanMetadata.addArgument(argument); */
        beanMetadata.addArgument(createValue(context, artifactId), null, 2);

        /*argument = new BeanArgumentImpl();
        argument.setIndex(3);
        argument.setValue(createValue(context, version));
        beanMetadata.addArgument(argument);*/
        beanMetadata.addArgument(createValue(context, version), null, 3);

        beanMetadata.setActivation(ComponentMetadata.ACTIVATION_LAZY);
        return beanMetadata;
    }
}
