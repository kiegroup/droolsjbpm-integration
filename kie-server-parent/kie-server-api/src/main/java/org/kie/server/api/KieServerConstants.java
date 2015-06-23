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

package org.kie.server.api;

public class KieServerConstants {

    public static final String LOCATION_HEADER = "Location";

    public static final String CLASS_TYPE_HEADER = "X-KIE-ClassType";
    public static final String KIE_CONTENT_TYPE_HEADER = "X-KIE-ContentType";

    public static final String CFG_PERSISTANCE_DS = "org.kie.server.persistence.ds";
    public static final String CFG_PERSISTANCE_TM = "org.kie.server.persistence.tm";
    public static final String CFG_PERSISTANCE_DIALECT = "org.kie.server.persistence.dialect";

    public static final String CFG_BYPASS_AUTH_USER = "org.kie.server.bypass.auth.user";
    public static final String CFG_HT_CALLBACK = "org.jbpm.ht.callback";
    public static final String CFG_HT_CALLBACK_CLASS = "org.jbpm.ht.custom.callback";

    public static final String CFG_KIE_MVN_SETTINGS = "kie.maven.settings.custom";

}
