/*
* Copyright 2012 JBoss Inc
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

import org.springframework.beans.factory.*;
import static org.drools.container.spring.beans.KnowledgeLoggerAdaptor.*;

public class KnowledgeLoggerBeanFactory implements
        FactoryBean<KnowledgeLoggerAdaptor>, InitializingBean, BeanNameAware, NamedBean, DisposableBean {

    protected KnowledgeLoggerAdaptor logger = null;
    private String name;
    private String beanName;
    String file;
    int interval;
    KNOWLEDGE_LOGGER_TYPE loggerType = KNOWLEDGE_LOGGER_TYPE.LOGGER_TYPES_CONSOLE;

    public KNOWLEDGE_LOGGER_TYPE getLoggerType() {
        return loggerType;
    }

    public void setLoggerType(KNOWLEDGE_LOGGER_TYPE loggerType) {
        this.loggerType = loggerType;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
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

    public void afterPropertiesSet() throws Exception {
        logger = new KnowledgeLoggerAdaptor(loggerType);
        if ( loggerType != KNOWLEDGE_LOGGER_TYPE.LOGGER_TYPES_CONSOLE) {
            logger.setFile(file);
            logger.setInterval(interval);
        }
    }

    public KnowledgeLoggerAdaptor getObject() throws Exception {
        return logger;
    }

    public Class getObjectType() {
        return KnowledgeLoggerAdaptor.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void destroy() throws Exception {
        try {
            logger.close();
        } catch(IllegalStateException ise) {
            //logger has been closed by the user...
            //ignore exception
        }
    }

}
