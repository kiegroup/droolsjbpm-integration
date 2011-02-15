package org.drools.grid.io;

public interface Message {

    public String getConversationId();

    public String getSenderId();

    public String getRecipientId();

    public int getRequestId();

    public int getResponseId();

    public Object getBody();

}