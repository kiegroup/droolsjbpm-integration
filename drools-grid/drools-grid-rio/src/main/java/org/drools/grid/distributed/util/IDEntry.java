/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.drools.grid.distributed.util;

/**
 *
 * @author salaboy
 */
public class IDEntry extends net.jini.entry.AbstractEntry{
    public String id;

   public IDEntry() {
       super();
   }

   public IDEntry(String id) {
       this. id = id;
   }
}
