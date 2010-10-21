package org.drools.grid.io.impl;

import java.io.Serializable;
import java.util.Map;

import org.drools.grid.io.Message;

public class MessageImpl
    implements
    Serializable,
    Message {
    private String   conversationId;
    private String   senderId;
    private String recipientId;
    private int      requestId;
    private int      responseId;
    private Object   body;

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

    //    public MessageImpl(String conversationId,
    //                       Map<String, Integer> contextVars,
    //                       Object payload) {
    //        this.conversationId = conversationId;
    //        this.responseId = -1;
    //        this.payload = payload;
    //    }

    /* (non-Javadoc)
     * @see org.drools.grid.io.impl.Message#getSessionId()
     */
    public String getConversationId() {
        return this.conversationId;
    }
    
    public String getSenderId() {
        return senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    /* (non-Javadoc)
     * @see org.drools.grid.io.impl.Message#getResponseId()
     */
    public int getRequestId() {
        return this.requestId;
    }
    
    public int getResponseId() {
        return this.responseId;
    }    

    /* (non-Javadoc)
     * @see org.drools.grid.io.impl.Message#getPayload()
     */
    public Object getBody() {
        return this.body;
    }

    /* (non-Javadoc)
     * @see org.drools.grid.io.impl.Message#toString()
     */
    @Override
    public String toString() {
        return "[Message conversationId=" + this.conversationId + "senderId=" + this.senderId + " requestId=" + this.requestId + " payload=" + this.body + "]";
    }
}
