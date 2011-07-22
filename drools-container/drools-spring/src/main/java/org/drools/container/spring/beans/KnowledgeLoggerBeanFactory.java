/*
* Copyright 2011 JBoss Inc
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

import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NamedBean;

public class KnowledgeLoggerBeanFactory implements
        FactoryBean, InitializingBean, BeanNameAware, NamedBean {

    public static enum KNOWLEDGE_LOGGER_TYPE {
        LOGGER_TYPE_FILE, LOGGER_TYPES_CONSOLE, LOGGER_TYPE_THREADED_FILE
    };
    
    private String name;
    private String beanName;
    private StatefulKnowledgeSession ksession;
    KnowledgeRuntimeLogger logger;
    String file;
    KNOWLEDGE_LOGGER_TYPE loggerType;

    public KNOWLEDGE_LOGGER_TYPE getLoggerType() {
        return loggerType;
    }

    public void setLoggerType(KNOWLEDGE_LOGGER_TYPE loggerType) {
        this.loggerType = loggerType;
    }

    public void setLoggerType(String loggerType) {
        this.loggerType = KNOWLEDGE_LOGGER_TYPE.valueOf(loggerType);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBeanName(String name) {
        beanName = name;
    }

    public String getBeanName() {
        return beanName;
    }

    public StatefulKnowledgeSession getKsession() {
        return ksession;
    }

    public void setKsession(StatefulKnowledgeSession ksession) {
        this.ksession = ksession;
    }

    public void afterPropertiesSet() throws Exception {
        if ( ksession == null ) {
            throw new IllegalArgumentException( "ksession property is mandatory" );
        }
        if ( name == null ) {
            name = beanName;
        }
        //System.out.println("KnowledgeLoggerBeanFactory::afterPropertiesSet - "+ksession);
        internalAfterPropertiesSet();
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Object getObject() throws Exception {
        return logger;
    }

    public Class getObjectType() {
        return KnowledgeRuntimeLogger.class;
    }

    public boolean isSingleton() {
        return true;
    }

    protected void internalAfterPropertiesSet() throws Exception {
        switch (loggerType) {
            case LOGGER_TYPE_FILE :
                logger = KnowledgeRuntimeLoggerFactory.newFileLogger(getKsession(), getFile());
                break;
            case LOGGER_TYPE_THREADED_FILE:
                logger = KnowledgeRuntimeLoggerFactory.newThreadedFileLogger(getKsession(), getFile(), 30);
                break;
            case LOGGER_TYPES_CONSOLE:
                logger = KnowledgeRuntimeLoggerFactory.newConsoleLogger(getKsession());
                break;
        }
    }

}
