/*
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

import org.drools.persistence.jpa.KnowledgeStoreServiceImpl;
import org.kie.api.persistence.jpa.KieStoreServices;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class KnowledgeStoreBeanFactory
    implements
    FactoryBean,
    InitializingBean {

    public Object getObject() throws Exception {
        return new KnowledgeStoreServiceImpl();
    }

    public Class< ? extends KieStoreServices> getObjectType() {
        return KieStoreServices.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
    }

}
