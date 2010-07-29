/**
 * Copyright 2010 JBoss Inc
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

package org.drools.container.spring.beans;

import org.drools.io.Resource;
import org.drools.io.impl.ClassPathResource;
import org.drools.io.impl.UrlResource;
import org.springframework.beans.factory.FactoryBean;

public class ResourceBeanFactory
    implements
    FactoryBean {

    private Resource resource;

    public ResourceBeanFactory(String source) {
        if ( source.trim().startsWith( "classpath:" ) ) {
            resource = new ClassPathResource( source.substring( source.indexOf( ':' ) + 1 ),
                                              ClassPathResource.class.getClassLoader() );
        } else {
            resource = new UrlResource( source );
        }
    }

    public Object getObject() throws Exception {
        return resource;
    }

    public Class<Resource> getObjectType() {
        return Resource.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
