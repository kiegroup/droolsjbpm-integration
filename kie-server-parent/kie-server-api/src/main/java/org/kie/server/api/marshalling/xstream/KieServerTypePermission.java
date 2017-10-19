/*
 * Copyright 2017 - 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.marshalling.xstream;

import static org.kie.server.api.KieServerConstants.SYSTEM_XSTREAM_ENABLED_PACKAGES;

import java.util.HashSet;
import java.util.Set;

import org.kie.internal.utils.KieTypeResolver;

import com.thoughtworks.xstream.security.WildcardTypePermission;

/**
 * Kie Server specific type permission implementation that allows:
 * <ul>
 *  <li>org.kie.server.api.model classes (including subpackages)</li>
 *  <li>classes that come from kjar or its dependencies (were loaded by kjar class loader)</li>
 *  <li>set of classes explicitly given when constructing this instance</li>
 *  <li>optionally defined by wildcard that is given via system property: org.kie.server.xstream.enabled.packages (comma separated list of wildcard expressions)</li>
 * </ul>
 *
 */
public class KieServerTypePermission extends WildcardTypePermission {
    
    private static final String[] WHITELISTED_PACKAGES = new String[]{
                                                                      "org.kie.server.api.model.**",
                                                                      "org.dashbuilder.dataset.filter.**",
                                                                      "org.dashbuilder.dataset.group.**",
                                                                      "org.dashbuilder.dataset.sort.**"
                                                                      
    };

    private final Set<Class<?>> classes;
    
    public KieServerTypePermission(Set<Class<?>> classes) {
        super(patterns());
        this.classes = classes == null ? new HashSet<>() : classes;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean allows(Class type) {
        // first check if any of the defined wildcards match
        boolean allowed = super.allows(type);
        
        if (!allowed) {            
            // next check if there were any classes explicitly given to the set of accepted classes
            allowed = classes.contains(type);
            // lastly check if given class was loaded by project/kjar class loader
            if (!allowed && type.getClassLoader() instanceof KieTypeResolver) {                    
                allowed = true;                
            }
        }
        
        return allowed;
    }

    protected static String[] patterns() {
        String packageList = System.getProperty(SYSTEM_XSTREAM_ENABLED_PACKAGES);
        String[] filter = new String[0];
        if (packageList != null) {            
            filter = packageList.split(",");            
        }
        
        String[] patterns = new String[WHITELISTED_PACKAGES.length + filter.length];
        System.arraycopy(filter, 0, patterns, 0, filter.length);
        System.arraycopy(WHITELISTED_PACKAGES, 0, patterns, filter.length, WHITELISTED_PACKAGES.length);
        
        return patterns;
    }
}
