/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model.taskassigning;

public class TaskStatus {

    public static final String Created = "Created";
    public static final String Ready = "Ready";
    public static final String Reserved = "Reserved";
    public static final String InProgress = "InProgress";
    public static final String Suspended = "Suspended";
    public static final String Completed = "Completed";
    public static final String Failed = "Failed";
    public static final String Error = "Error";
    public static final String Exited = "Exited";
    public static final String Obsolete = "Obsolete";
}