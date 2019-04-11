/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.jms;

public class JMSConstants {

    public static final String SERIALIZATION_FORMAT_PROPERTY_NAME = "serialization_format";
    public static final String CONTAINER_ID_PROPERTY_NAME = "container_id";
    public static final String CONVERSATION_ID_PROPERTY_NAME = "kie_conversation_id";

    public static final String CLASS_TYPE_PROPERTY_NAME = "kie_class_type";

    public static final String TARGET_CAPABILITY_PROPERTY_NAME = "kie_target_capability";
    public static final String USER_PROPERTY_NAME = "kie_user";
    public static final String PASSWRD_PROPERTY_NAME = "kie_password";

    public static final String INTERACTION_PATTERN_PROPERTY_NAME = "kie_interaction_pattern";

    // from 1 to 99 means response should be sent from the server
    public static final int UPPER_LIMIT_REPLY_INTERACTION_PATTERNS = 100;

    public static final int REQUEST_REPLY_PATTERN = 1;
    public static final int ASYNC_REPLY_PATTERN = 2;

    // from 100 up means server should not send any response
    public static final int FIRE_AND_FORGET_PATTERN = 101;
}
