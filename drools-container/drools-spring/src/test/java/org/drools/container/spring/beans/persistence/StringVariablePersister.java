/**
 * Copyright 2010 JBoss Inc
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
public class StringVariablePersister
    implements
    VariablePersister {

    public VariableInstanceInfo persistExternalVariable(String name,
                                                        Object o,
                                                        VariableInstanceInfo oldValue,
                                                        Environment env) {
        if ( o == null || (oldValue != null && oldValue.getPersister().equals( "" )) ) {
            return null;
        }
        try {
            boolean newVariable = false;
            EntityManager em = (EntityManager) env.get( EnvironmentName.CMD_SCOPED_ENTITY_MANAGER );
            StringPersistedVariable result = null;
            if ( oldValue instanceof StringPersistedVariable ) {
                result = (StringPersistedVariable) oldValue;
            }
            if ( result == null ) {
                result = new StringPersistedVariable();

                newVariable = true;
            }
            result.setPersister( this.getClass().getName() );
            result.setName( name );
            // entity might have changed, updating info
            result.setString( (String) o );
            if ( newVariable ) {
                em.persist( result );
            } else {
                em.merge( result );
            }
            System.out.println( "Saving StringPersistedVariable id=" + result.getId() + " string=" + result.getString() );
            return result;
        } catch ( Throwable t ) {
            Logger.getLogger( JPAVariablePersister.class.getName() ).log( Level.SEVERE,
                                                                          null,
                                                                          t );
            throw new RuntimeException( "Could not persist external variable",
                                        t );
        }

    }

	public Object getExternalPersistedVariable(VariableInstanceInfo variableInstanceInfo, Environment env) {
		env.get(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER);
		if (variableInstanceInfo == null) {
			return null;
		}
		return ((StringPersistedVariable) variableInstanceInfo).getString();
	}
}
