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

package org.jbpm.simulation.impl;

public class SystemOutLogger {

    private boolean log = false;
    
    public SystemOutLogger() {
        String configuredByProperty = System.getProperty("jbpm.simulation.log.enabled");
        if ("true".equalsIgnoreCase(configuredByProperty)) {
            this.log = true;
        }
    }
    
    public void log(String message) {
        if (log) {
            System.out.println("SIMULATION-->" + message);
        }
    }

    public boolean isLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }
}
