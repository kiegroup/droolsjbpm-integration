package org.drools.grid.io.impl;

import java.io.Serializable;

public class ExceptionMessage extends MessageImpl
    implements
    Serializable{

    public ExceptionMessage(String conversationId, String senderId, String recipientId, int requestId, int responseId, Throwable body) {
        super(conversationId, senderId, recipientId, requestId, responseId, body);
    }
    
    @Override
    public Throwable getBody() {
        return (Throwable) super.getBody();
    }

    public void setBody(Throwable body) {
        super.setBody(body);
    }

    /* (non-Javadoc)
     * @see org.drools.grid.io.impl.Message#toString()
     */

    @Override
    public String toString() {
        return "ExceptionMessage{" + "conversationId=" + this.getConversationId() + ", senderId=" + this.getSenderId() + ", recipientId=" + this.getRecipientId() + ", requestId=" + this.getRequestId() + ", responseId=" + this.getResponseId() + ", body=" + this.getBody() + '}';
    }
  
}
