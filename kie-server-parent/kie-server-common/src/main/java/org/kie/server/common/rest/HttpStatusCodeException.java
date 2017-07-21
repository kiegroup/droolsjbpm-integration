/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.common.rest;

import java.util.List;
import java.util.ArrayList;

/**
 * Provides static lists of exceptions which map to a particular HTTP status codes. This is internal API that should be
 * used when REST requests made to Kie Server throw exceptions.
 */
public class HttpStatusCodeException {

    // Exceptions which return HTTP Status code 400
    public static final List<Class> BAD_REQUEST = new ArrayList<>();

    static {
        BAD_REQUEST.add( NumberFormatException.class );
        BAD_REQUEST.add( IllegalArgumentException.class );
    }

}
