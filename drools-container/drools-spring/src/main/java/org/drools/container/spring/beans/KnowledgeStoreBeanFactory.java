package org.drools.container.spring.beans;

import org.drools.persistence.jpa.KnowledgeStoreService;
import org.drools.persistence.jpa.impl.KnowledgeStoreServiceImpl;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class KnowledgeStoreBeanFactory
    implements
    FactoryBean,
    InitializingBean {

    public Object getObject() throws Exception {
        return new KnowledgeStoreServiceImpl();
    }

    public Class< ? extends KnowledgeStoreService> getObjectType() {
        return KnowledgeStoreService.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
    }

}
