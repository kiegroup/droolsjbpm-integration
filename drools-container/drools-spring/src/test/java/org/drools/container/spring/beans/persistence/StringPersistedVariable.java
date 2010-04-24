/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.drools.container.spring.beans.persistence;

import javax.persistence.Entity;
import org.drools.persistence.processinstance.variabletypes.VariableInstanceInfo;

/**
 *
 * @author salaboy
 * @author baunax@gmail.com
 */
@Entity
public class StringPersistedVariable extends VariableInstanceInfo {

    private String string;

    public StringPersistedVariable() {
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }



}
