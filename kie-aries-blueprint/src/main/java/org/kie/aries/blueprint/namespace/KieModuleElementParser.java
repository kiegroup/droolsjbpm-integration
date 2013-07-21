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
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.concurrent.Callable;

public class KieModuleElementParser extends AbstractElementParser {
    @Override
    public Metadata parseElement(ParserContext context, Element element) {
        String id = getId(context, element);

        MutableBeanMetadata beanMetadata = (MutableBeanMetadata) context.createMetadata(BeanMetadata.class);
        beanMetadata.setClassName("org.kie.aries.blueprint.factorybeans.KieObjectsFactoryBean");
        beanMetadata.setFactoryMethod("createKieStore");
        beanMetadata.setId(id);

        String prefix = element.getPrefix();
        NodeList kbaseNodeList = element.getElementsByTagName(prefix+":kbase");
        if (kbaseNodeList != null) {
            for (int i=0; i < kbaseNodeList.getLength(); i++){
                Node kbaseNode = kbaseNodeList.item(i);
                if (kbaseNode instanceof Element) {
                    Element kbaseElement = (Element) kbaseNode;
                    kbaseElement.setAttribute("id", kbaseElement.getAttribute("name"));
                    context.getComponentDefinitionRegistry().registerComponentDefinition(new KieBaseElementParser().parseElement(context, kbaseElement));
                }
            }
        }
        return beanMetadata;
    }

    public static class PassThroughCallable<T> implements Callable<T> {

        private T value;

        public PassThroughCallable(T value) {
            this.value = value;
        }

        public T call() throws Exception {
            return value;
        }
    }

}
