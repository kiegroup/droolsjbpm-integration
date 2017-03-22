/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.camel;

public class KieCamelConstants {

    public static final String KIE_HEADERS_PREFIX = "CamelKie";

    public static final String KIE_CLIENT = KIE_HEADERS_PREFIX + "Client";
    public static final String KIE_OPERATION = KIE_HEADERS_PREFIX + "Operation";

    public static final String RESPONSE_TYPE = KIE_HEADERS_PREFIX + "ResponseType";
    public static final String RESPONSE_MESSAGE = KIE_HEADERS_PREFIX + "ResponseMessage";

}
