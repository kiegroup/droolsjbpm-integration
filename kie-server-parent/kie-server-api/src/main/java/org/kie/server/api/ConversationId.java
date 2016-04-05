/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.api;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.UUID;

import org.kie.server.api.model.ReleaseId;

/**
 * ConversationId represents unique conversation between client and server that comes with valuable information
 * about the conversation itself:
 * <ul>
 *     <li>identifier of kie server</li>
 *     <li>container id</li>
 *     <li>release id (GAV resolved one)</li>
 *     <li>unique UUID string</li>
 * </ul>
 */
public class ConversationId {

    private String kieServerId;
    private String containerId;
    private ReleaseId releaseId;
    private String uniqueString;

    private ConversationId(String kieServerId, String containerId, ReleaseId releaseId) {
        this (kieServerId, containerId, releaseId, UUID.randomUUID().toString());
    }

    private ConversationId(String kieServerId, String containerId, ReleaseId releaseId, String uniqueString) {
        this.kieServerId = kieServerId;
        this.containerId = containerId;
        this.releaseId = releaseId;
        this.uniqueString = uniqueString;

        validate();
    }

    public static ConversationId from(String kieServerId, String containerId, ReleaseId releaseId) {
        return new ConversationId(kieServerId, containerId, releaseId);
    }

    public static ConversationId fromString(String conversationIdString) {
        try {
            String conversationId = URLDecoder.decode(conversationIdString, "UTF-8");

            String[] conversationIdElements = conversationId.split(":");
            if (conversationIdElements.length != 6) {
                throw new IllegalArgumentException("Non-parsable conversationId '" + conversationIdString + "'");
            }

            String kieServerId = conversationIdElements[0];
            String containerId = conversationIdElements[1];
            ReleaseId releaseId = new ReleaseId(conversationIdElements[2], conversationIdElements[3], conversationIdElements[4]);
            String uniqueString = conversationIdElements[5];

            return new ConversationId(kieServerId, containerId, releaseId, uniqueString);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getKieServerId() {
        return kieServerId;
    }

    public String getContainerId() {
        return containerId;
    }

    public ReleaseId getReleaseId() {
        return releaseId;
    }

    public String getUniqueString() {
        return uniqueString;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(kieServerId)
        .append(":")
        .append(containerId)
        .append(":")
        .append(releaseId.toExternalForm())
        .append(":")
        .append(uniqueString);

        try {
            return URLEncoder.encode(builder.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected void validate() {
        if (kieServerId == null || kieServerId.isEmpty()) {
            throw new IllegalArgumentException("ConversationId not valid - missing kieServerId");
        }
        if (containerId == null || containerId.isEmpty()) {
            throw new IllegalArgumentException("ConversationId not valid - missing containerId");
        }
        if (releaseId == null) {
            throw new IllegalArgumentException("ConversationId not valid - missing releaseId");
        }
        if (uniqueString == null || uniqueString.isEmpty()) {
            throw new IllegalArgumentException("ConversationId not valid - missing uniqueString");
        }
    }
}
