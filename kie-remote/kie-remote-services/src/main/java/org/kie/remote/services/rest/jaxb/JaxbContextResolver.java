/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.remote.services.rest.jaxb;

import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * We use this class in order to use (user) classes from Kjar deployments in serialization.
 * </p>
 * Users may send inputs to the REST API that contain instances of these classes as parameters to {@link KieSession} operations.
 */
@Provider
public class JaxbContextResolver implements ContextResolver<JAXBContext> {

    private static final Logger logger = LoggerFactory.getLogger(JaxbContextResolver.class);

    @Inject
    DynamicJaxbContextManager dynamicContextManager;

    @PostConstruct
    public void configure() {
        if (dynamicContextManager == null) {
            logger.info("JaxbContextResolver does not support CDI injection, looking up DynamicJaxbContext programmatically");
            BeanManager beanManager = BeanManagerProvider.getInstance().getBeanManager();
            try {
                Set<Bean<?>> beans = beanManager.getBeans( DynamicJaxbContextManager.class );

                if (beans != null && !beans.isEmpty()) {
                    Bean<?> bean = (Bean<?>) beans.iterator().next();

                    dynamicContextManager = (DynamicJaxbContextManager)
                            beanManager.getReference(bean,
                                                     DynamicJaxbContextManager.class,
                                                     beanManager.createCreationalContext(bean));
                }

            } catch (Exception e) {
                logger.warn("Unable to retrieve DynamicJaxbContext programmatically from cdi container due to {}", e.getMessage());
            }
        }
    }

    @Override
    public JAXBContext getContext(Class<?> type) {
        return dynamicContextManager.getJaxbContext();
    }

}
