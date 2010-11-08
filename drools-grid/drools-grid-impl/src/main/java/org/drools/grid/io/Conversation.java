/**
 * 
 */
package org.drools.grid.io;

public interface Conversation {

    void respond(Object body);

    void sendMessage(Object body,
                     MessageReceiverHandler handler);

}