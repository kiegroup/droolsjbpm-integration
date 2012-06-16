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

import org.drools.logger.KnowledgeRuntimeLogger;

import java.io.Serializable;

public class KnowledgeLoggerAdaptor implements Serializable {
    public static enum KNOWLEDGE_LOGGER_TYPE {
        LOGGER_TYPE_FILE, LOGGER_TYPES_CONSOLE, LOGGER_TYPE_THREADED_FILE
    }

    String file;
    int interval;
    KNOWLEDGE_LOGGER_TYPE loggerType = KNOWLEDGE_LOGGER_TYPE.LOGGER_TYPES_CONSOLE;
    KnowledgeRuntimeLogger runtimeLogger;

    public KnowledgeLoggerAdaptor(KNOWLEDGE_LOGGER_TYPE loggerType){
        setLoggerType(loggerType);
    }

    public KNOWLEDGE_LOGGER_TYPE getLoggerType() {
        return loggerType;
    }

    protected void setLoggerType(KNOWLEDGE_LOGGER_TYPE loggerType) {
        this.loggerType = loggerType;
    }

    public int getInterval() {
        return interval;
    }

    protected void setInterval(int interval) {
        this.interval = interval;
    }

    public String getFile() {
        return file;
    }

    protected void setFile(String file) {
        this.file = file;
    }

    protected void setRuntimeLogger(KnowledgeRuntimeLogger runtimeLogger){
        this.runtimeLogger = runtimeLogger;
    }

    public KnowledgeRuntimeLogger getRuntimeLogger(){
        return runtimeLogger;
    }

    public void close() {
        if ( runtimeLogger != null) {
            runtimeLogger.close();
        }
    }
}
