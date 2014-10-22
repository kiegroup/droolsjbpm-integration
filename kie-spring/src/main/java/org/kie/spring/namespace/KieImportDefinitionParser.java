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
package org.kie.spring.namespace;


import org.kie.spring.factorybeans.KieImportFactoryBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class KieImportDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String ATTRIBUTE_RELEASEID_REF = "releaseId-ref";
    private static final String ATTRIBUTE_SCANNER_ENABLED = "enableScanner";
    private static final String ATTRIBUTE_SCANNER_INTERVAL = "scannerInterval";

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(KieImportFactoryBean.class);

        String releaseIdRef = element.getAttribute(ATTRIBUTE_RELEASEID_REF);
        if ((releaseIdRef != null && releaseIdRef.trim().length() > 0)){
            factory.addPropertyReference("releaseId", releaseIdRef);
            factory.addPropertyValue("releaseIdName", releaseIdRef);
        }

        String scannerEnabled = element.getAttribute(ATTRIBUTE_SCANNER_ENABLED);
        final boolean scannerEnabledValue = "true".equalsIgnoreCase(scannerEnabled);
        if (scannerEnabledValue) {
            factory.addPropertyValue("scannerEnabled", true);
            String scanningInterval = element.getAttribute(ATTRIBUTE_SCANNER_INTERVAL);
            if (scanningInterval != null) {
                try {
                    int interval = Integer.parseInt(scanningInterval);
                    factory.addPropertyValue(ATTRIBUTE_SCANNER_INTERVAL, interval);
                } catch (Exception e) {
                    //will never happen as the XSD would prevent non-integers
                    throw new IllegalArgumentException("Scanner Interval attribute must be of type integer.");
                }
            }
        }

        return factory.getBeanDefinition();
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
}