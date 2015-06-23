/*
 * Copyright 2015 JBoss Inc
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

package org.kie.services.shared;

public class KieRemoteWebServiceContstants {

    private KieRemoteWebServiceContstants() {
       // no public constructor: constants class
    }

    /*
     * Namespaces
     */
    public static final String WS_ADDR_NAMESPACE = "http://www.w3.org/2005/08/addressing";

    public static final String WS_SECURITY_UTILITY_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    /*
     * Header ids
     */
    public static final String MESSAGE_ID = "MessageID";

    public static final String RELATES_TO = "RelatesTo";

    public static final String REPLY_TO = "ReplyTo";
}
