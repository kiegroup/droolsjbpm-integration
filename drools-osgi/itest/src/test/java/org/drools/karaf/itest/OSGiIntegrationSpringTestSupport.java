/*
 * Copyright 2012 Red Hat
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.drools.karaf.itest;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

public abstract class OSGiIntegrationSpringTestSupport extends KarafIntegrationTestSupport {

    protected OsgiBundleXmlApplicationContext applicationContext;
    protected abstract OsgiBundleXmlApplicationContext createApplicationContext();

    @Override
    protected CamelContext createCamelContext() throws Exception {
        setThreadContextClassLoader();
        applicationContext = createApplicationContext();
        assertNotNull("Should have created a valid spring context", applicationContext);
        applicationContext.setBundleContext(bundleContext);
        applicationContext.refresh();
        String[] names = applicationContext.getBeanNamesForType(SpringCamelContext.class);
        if (names.length == 1) {
            return applicationContext.getBean(names[0], SpringCamelContext.class);
        } else {
            throw new IllegalStateException("Exactly 1 bean of type SpringCamelContext expected but found " + names.length + " beans.");
        }
    }

}
