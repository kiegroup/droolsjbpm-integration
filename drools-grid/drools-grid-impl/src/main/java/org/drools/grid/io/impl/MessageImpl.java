package org.drools.grid.io.impl;

import java.io.Serializable;
import java.util.Map;

import org.drools.grid.io.Message;

public class MessageImpl
    implements
    Serializable,
    Message {
    private String conversationId;
    private String senderId;
    private String recipientId;
    private int    requestId;
    private int    responseId;
    private Object body;

    public MessageImpl() {
        
    }
    
    public MessageImpl(String conversationId,
                       String senderId,
                       String recipientId,
                       int requestId,
                       int responseId,
                       Object body) {
        this.conversationId = conversationId;
        this.requestId = requestId;
        this.recipientId = recipientId;
        this.responseId = responseId;
        this.body = body;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getResponseId() {
        return responseId;
    }

    public void setResponseId(int responseId) {
        this.responseId = responseId;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    /* (non-Javadoc)
     * @see org.drools.grid.io.impl.Message#toString()
     */
    @Override
    public String toString() {
        return "[Message conversationId=" + this.conversationId + "senderId=" + this.senderId + " requestId=" + this.requestId + " payload=" + this.body + "]";
    }
}
