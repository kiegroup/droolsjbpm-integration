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
import org.drools.core.util.StringUtils;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.w3c.dom.Element;

public class KieImportElementParser extends AbstractElementParser {

    @Override
    public ComponentMetadata parseElement( ParserContext context, Element element ) {
        String id = getId(context, element);

        MutableBeanMetadata beanMetadata = (MutableBeanMetadata) context.createMetadata( BeanMetadata.class );
        beanMetadata.setClassName("org.kie.aries.blueprint.factorybeans.KieObjectsFactoryBean");
        beanMetadata.setFactoryMethod("createImport");
        beanMetadata.setId(id);

        addBundleContextProperty(beanMetadata, context);

        String releaseIdRef = element.getAttribute("releaseId-ref");
        beanMetadata.addArgument(createValue(context, releaseIdRef), null, 0);

        if (!StringUtils.isEmpty(releaseIdRef)) {
            beanMetadata.addArgument(createRef(context, releaseIdRef), null, 1);
        } else {
            beanMetadata.addArgument(createNullMetadata(), null, 1);
        }

        String enableScanner = element.getAttribute("enableScanner");
        beanMetadata.addArgument(createValue(context, "true".equals( enableScanner )), null, 2);

        String scannerInterval = element.getAttribute("scannerInterval");
        long interval = -1L;
        if (!StringUtils.isEmpty( scannerInterval )) {
            try {
                interval = Long.parseLong( scannerInterval );
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("scannerInterval must be positive");
            }
        }
        beanMetadata.addArgument(createValue(context, interval), null, 3);

        beanMetadata.setActivation(ComponentMetadata.ACTIVATION_EAGER);

        return beanMetadata;
    }
}
