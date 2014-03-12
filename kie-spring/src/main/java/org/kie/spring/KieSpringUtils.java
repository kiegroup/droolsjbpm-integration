/*
 * Copyright 2013 JBoss Inc
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

package org.kie.spring;


import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

public final class KieSpringUtils {

    private static ReleaseId defaultReleaseId;

    public static ApplicationContext getSpringContext(ReleaseId releaseId) {
        return applicationContextMap.get(releaseId==null?defaultReleaseId:releaseId);
    }

    public static ApplicationContext getDefaultSpringContext() {
        return getSpringContext(null);
    }

    static Map<ReleaseId, ApplicationContext> applicationContextMap = new HashMap<ReleaseId, ApplicationContext>();

    static void setReleaseIdForContext(ReleaseId releaseId, ApplicationContext applicationContext){
        applicationContextMap.put(releaseId, applicationContext);
    }

    static KieServices ks;
    static {
        //this forces the KieModules to be loaded up.
        ks = KieServices.Factory.get();
        KieContainer kContainer = ks.newKieClasspathContainer();
    }

    static void setDefaultReleaseId(ReleaseId releaseId) {
        defaultReleaseId = releaseId;
    }
}