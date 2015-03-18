/*
 * Copyright 2014 JBoss Inc
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
package org.kie.integration.eap.maven.util;

public class EAPConstants {

    public static final String GROUP_ID = "groupId";

    public static final String ARTIFACT_ID = "artifactId";

    public static final String VERSION = "version";

    public static final String PACKAGING = "packaging";

    public static final String SCOPE_COMPILE = "compile";

    public static final String SCOPE_PROVIDED = "provided";

    public static final String SCOPE_TEST = "test";

    public static final String POM = "pom";

    public static final String ARTIFACT_SEPARATOR = ":";

    /** The project version maven propoerty. **/
    public static final String PROPERTY_PROJECT_VERSION = "project.version";

    public static final String LAYER_BASE = "base";

    /** The preffix for the module properties. **/
    public static final String MODULE_PROPERTY_PREFFIX = "module.";

    /** The preffix for the module patch properties. **/
    public static final String MODULE_PATCH_PREFFIX = "module.patch.";
    
    /** The property for the module name in a pom's module artifact. **/
    public static final String MODULE_NAME = "module.name";

    /** The property for the module location in a pom's module artifact. **/
    public static final String MODULE_LOCATION= "module.location";

    /** The property for the module type in a pom's module artifact. **/
    public static final String MODULE_TYPE = "module.type";

    /** The property for the module slot in a pom's module artifact. **/
    public static final String MODULE_SLOT = "module.slot";

    /** The property for the static module dependencies in a pom's module artifact. **/
    public static final String MODULE_DEPENDENCIES = "module.dependencies";

    /** The property for the dynamic module dependency in a pom's module artifact. **/
    public static final String MODULE_ADD_JBOSS_ALL= "module.add-jboss-all";

    public static final String MODULE_TYPE_STATIC = "static";

    public static final String MODULE_TYPE_DYNAMIC = "dynamic";

    public static final String MODULE_TYPE_BASE = "base";

    public static final String MODULE_SERVICES_IMPORT = "import";

    public static final String WAR = "war";

    public static final String EXCLUSIONS = "exclusions";

    public static final String COMMA = ",";

    public static final String DISTRO_PACKAGE_FILE_NAME = "distribution.xml";

    public static final String DISTRIBUTION_PROPERTIES_PACKAGE = "org.kie.integration.eap.maven.distributions";
    
    public static final String WEB_INF = "WEB-INF";

    public static final String WEB_INF_LIB = "WEB-INF/lib";

    public static final String META_INF = "META-INF";

    public static final String NEW_LINE = "\n";

    public static final String ALL = "ALL";

    public static final String SLOT_MAIN = "main";

    public static final String PATCHES_PATH = "patches";
}
