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

package org.drools.grid.generic;

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
        return sessionId;
    }

    public int getResponseId() {
        return responseId;
    }

    public boolean isAsync() {
        return async;
    }

    public Object getPayload() {
        return payload;
    }

    public String toString() {
        return "sessionId=" + this.sessionId + " responseId=" + responseId + " async=" + this.async + " payload=" + this.payload;
    }

}
