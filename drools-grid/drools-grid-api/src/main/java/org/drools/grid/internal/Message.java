package org.drools.grid.internal;

import java.io.Serializable;
import java.util.Map;

public class Message
    implements
    Serializable {
    private int     sessionId;
    private int     responseId;
    private boolean async;
    private Object  payload;

    public Message(int sessionId,
                   int responseId,
                   boolean async,
                   Object payload) {
        this.sessionId = sessionId;
        this.async = async;
        this.responseId = responseId;
        this.payload = payload;
    }

    public Message(int sessionId,
                   Map<String, Integer> contextVars,
                   Object payload) {
        this.sessionId = sessionId;
        this.responseId = -1;
        this.payload = payload;
    }

    public int getSessionId() {
        return this.sessionId;
    }

    public int getResponseId() {
        return this.responseId;
    }

    public boolean isAsync() {
        return this.async;
    }

    public Object getPayload() {
        return this.payload;
    }

    @Override
    public String toString() {
        return "sessionId=" + this.sessionId + " responseId=" + this.responseId + " async=" + this.async + " payload=" + this.payload;
    }

}
