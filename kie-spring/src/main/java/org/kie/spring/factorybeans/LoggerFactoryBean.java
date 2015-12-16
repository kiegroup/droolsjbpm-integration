/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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
package org.kie.spring.factorybeans;

import org.kie.spring.factorybeans.LoggerAdaptor.KNOWLEDGE_LOGGER_TYPE;
import org.springframework.beans.factory.*;

public class LoggerFactoryBean implements
        FactoryBean<LoggerAdaptor>, InitializingBean, BeanNameAware, NamedBean, DisposableBean {

    protected LoggerAdaptor logger = null;
    private String name;
    private String beanName;
    String file;
    int interval;
    KNOWLEDGE_LOGGER_TYPE loggerType = KNOWLEDGE_LOGGER_TYPE.LOGGER_TYPE_CONSOLE;

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
        logger = new LoggerAdaptor(loggerType);
        if (loggerType != KNOWLEDGE_LOGGER_TYPE.LOGGER_TYPE_CONSOLE) {
            logger.setFile(file);
            logger.setInterval(interval);
        }
    }

    public LoggerAdaptor getObject() throws Exception {
        return logger;
    }

    public Class getObjectType() {
        return LoggerAdaptor.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void destroy() throws Exception {
        try {
            logger.close();
        } catch (IllegalStateException ise) {
            //logger has been closed by the user...
            //ignore exception
        }
    }

}
