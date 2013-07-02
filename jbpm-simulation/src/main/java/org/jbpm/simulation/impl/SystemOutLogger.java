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
