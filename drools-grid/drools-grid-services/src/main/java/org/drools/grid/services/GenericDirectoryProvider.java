package org.drools.grid.services;

import java.util.HashMap;
import java.util.Map;
import org.drools.grid.GenericNodeConnector;

/**
 * @author salaboy
 */
public abstract class GenericDirectoryProvider {
    protected Map<String, Object> parameters = new HashMap<String, Object>();


    public abstract GenericNodeConnector getDirectoryConnector();
 
    
    
    public void setParameter(String name, Object value){
        this.parameters.put(name, value);
    }
    public Object getParameter(String name){
        return this.parameters.get(name);
    }
    public Map<String, Object> getParameters(){
        return this.parameters;
    }

    public abstract DirectoryInstance getDirectoryInstance(String name);

    public abstract String getId();
}
