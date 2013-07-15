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


import org.drools.compiler.kie.builder.impl.FileKieModule;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.net.URL;
import java.util.Collection;

public final class InternalKieSpringUtils {
    public static ApplicationContext getSpringContext(ReleaseId releaseId, URL kModuleSpringFileLocation, File baseDirectory) {
        KModuleSpringMarshaller.fromXML(kModuleSpringFileLocation, baseDirectory.getAbsolutePath(), releaseId);
        return  KieSpringUtils.applicationContextMap.get(releaseId);
    }

    static {
        KieSpringUtils.getDefaultSpringContext();
    }
}