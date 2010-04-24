/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.drools.container.spring.beans.persistence;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.drools.persistence.processinstance.persisters.JPAVariablePersister;
import org.drools.persistence.processinstance.persisters.VariablePersister;
import org.drools.persistence.processinstance.variabletypes.VariableInstanceInfo;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;

/**
 *
 * @author salaboy
 */
public class StringVariablePersister implements VariablePersister {

    public VariableInstanceInfo persistExternalVariable(String name, Object o, VariableInstanceInfo oldValue, Environment env) {
        if (o == null || (oldValue != null && oldValue.getPersister().equals(""))) {
            return null;
        }
        try { 
            boolean newVariable = false;
            EntityManager em = (EntityManager) env.get(EnvironmentName.ENTITY_MANAGER);
            StringPersistedVariable result = null;
            if (oldValue instanceof StringPersistedVariable) {
                result = (StringPersistedVariable) oldValue;
            }
            if (result == null) {
                result = new StringPersistedVariable();

                newVariable = true;
            }
            result.setPersister(this.getClass().getName());
            result.setName(name);
            // entity might have changed, updating info
            result.setString((String)o);
            if (newVariable) {
                em.persist(result);
            } else {
                em.merge(result);
            }
            System.out.println("Saving StringPersistedVariable id=" + result.getId() + " string=" + result.getString() );
            return result;
        } catch (Throwable t) {
            Logger.getLogger(JPAVariablePersister.class.getName()).log(Level.SEVERE, null, t);
            throw new RuntimeException("Could not persist external variable", t);
        }

    }

    public Object getExternalPersistedVariable(VariableInstanceInfo variableInstanceInfo, Environment env) {
        EntityManager em = (EntityManager) env.get(EnvironmentName.ENTITY_MANAGER);
                if(((StringPersistedVariable) variableInstanceInfo) == null){
                    return null;
                }else{

                    return ((StringPersistedVariable) variableInstanceInfo).getString();
                }

    }
}
