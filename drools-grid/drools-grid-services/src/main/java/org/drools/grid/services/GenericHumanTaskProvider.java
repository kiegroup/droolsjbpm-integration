package org.drools.grid.services;

import java.util.HashMap;
import java.util.Map;
import org.drools.grid.GenericHumanTaskConnector;

/**
 * @author salaboy
 */
public abstract class GenericHumanTaskProvider {
    protected Map<String, Object> parameters = new HashMap<String, Object>();


    public abstract GenericHumanTaskConnector getHumanTaskConnector();

    
    
    public void setParameter(String name, Object value){
        this.parameters.put(name, value);
    }
    public Object getParameter(String name){
        return this.parameters.get(name);
    }
    public Map<String, Object> getParameters(){
        return this.parameters;
    }

    public abstract TaskServerInstance getTaskServerInstance(String name);

    public abstract String getId();
}
