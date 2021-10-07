/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.spring.boot.autoconfiguration.audit.replication;

public final class MessageType {

    private MessageType() {
        // private
    }

    public static final int PROCESS_START_EVENT_TYPE = 100;
    public static final int PROCESS_COMPLETED_EVENT_TYPE = 101;
    public static final int NODE_SCHEDULED_EVENT_TYPE = 107;
    public static final int NODE_ENTER_EVENT_TYPE = 102;
    public static final int NODE_LEFT_EVENT_TYPE = 103;
    public static final int VAR_CHANGE_EVENT_TYPE = 104;
    public static final int NODE_SLA_VIOLATED = 105;
    public static final int PROCESS_SLA_VIOLATED = 106;

    public static final int TASK_STARTED = 201;
    public static final int TASK_ACTIVATED = 202;
    public static final int TASK_CLAIMED = 203;
    public static final int TASK_SKIPPED = 204;
    public static final int TASK_STOPPED = 205;
    public static final int TASK_COMPLETED = 206;
    public static final int TASK_FAILED = 207;
    public static final int TASK_EVENT = 208;
    public static final int TASK_ADDED = 209;
    public static final int TASK_EXITED = 210;
    public static final int TASK_RELEASED = 211;
    public static final int TASK_RESUMED = 212;
    public static final int TASK_SUSPENDED = 213;
    public static final int TASK_FORWARDED = 214;
    public static final int TASK_DELEGATED = 215;
    public static final int TASK_NOMINATED = 216;
    public static final int TASK_UPDATED = 217;
    public static final int TASK_REASSIGNED = 218;
    public static final int TASK_VAR_OUT_CHANGED = 219;
    public static final int TASK_VAR_IN_CHANGED = 210;

    public static final int BAM_TASK_EVENT = 301;

}
