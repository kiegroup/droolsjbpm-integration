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

package org.kie.server.client;

/**
 * Responsible for providing user credentials that can be obtained in various ways
 * that depends on the implementation.
 */
public interface CredentialsProvider {

    public static final String BASIC_AUTH_PREFIX = "Basic ";
    public static final String TOKEN_AUTH_PREFIX = "Bearer ";

    /**
     * Returns name of the HTTP header to be set with given authorization
     * @return
     */
    String getHeaderName();

    /**
     * Returns authorization string that shall be used for setting up
     * "Authorization" header for HTTP communication. It's expected to be completely setup
     * including prefix (such as Basic) and encryption (such as Base64 in case of basic) if needed.
     * @return
     */
    String getAuthorization();
}
