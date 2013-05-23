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
package org.kie.aries.blueprint.factorybeans;

import org.kie.api.logger.KieRuntimeLogger;

import java.io.Serializable;

public class KieLoggerAdaptor implements Serializable {
    public static enum KNOWLEDGE_LOGGER_TYPE {
        LOGGER_TYPE_FILE, LOGGER_TYPE_CONSOLE, LOGGER_TYPE_THREADED_FILE
    }

    String file;
    int interval;
    KNOWLEDGE_LOGGER_TYPE loggerType = KNOWLEDGE_LOGGER_TYPE.LOGGER_TYPE_CONSOLE;
    KieRuntimeLogger runtimeLogger;

    public KieLoggerAdaptor() {
    }

    public KieLoggerAdaptor(KNOWLEDGE_LOGGER_TYPE loggerType) {
        setLoggerType(loggerType);
    }

    public String getLoggerType() {
        return loggerType.toString();
    }

    protected void setLoggerType(KNOWLEDGE_LOGGER_TYPE loggerType) {
        this.loggerType = loggerType;
    }

    public void setLoggerType(String loggerType) {
        this.loggerType = KNOWLEDGE_LOGGER_TYPE.valueOf(loggerType);
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

    protected void setRuntimeLogger(KieRuntimeLogger runtimeLogger) {
        this.runtimeLogger = runtimeLogger;
    }

    public KieRuntimeLogger getRuntimeLogger() {
        return runtimeLogger;
    }

    public void close() {
        if (runtimeLogger != null) {
            runtimeLogger.close();
        }
    }
}
