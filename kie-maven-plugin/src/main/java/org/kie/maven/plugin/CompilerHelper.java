/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
package org.kie.maven.plugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.drools.compiler.kie.builder.impl.FileKieModule;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieMetaInfoBuilder;
import org.drools.core.common.ProjectClassLoader;
import org.drools.core.rule.KieModuleMetaInfo;
import org.drools.core.rule.TypeMetaInfo;

//Contains the logic used by the Maven compiler in the BuildMojo to permits better tests

class CompilerHelper {

    public String getCompilationID(Map<String, Object> optionalKieMap, Log log) {
        Object compilationIDObj = optionalKieMap.get("compilation.ID");
        if(compilationIDObj != null){
            return compilationIDObj.toString();
        }else{
            log.error("compilation.ID key not present in the shared map using thread name:"
                                   + Thread.currentThread().getName());
            return Thread.currentThread().getName();
        }
    }

    public void shareKieObjectsWithMap(InternalKieModule kModule, String compilationID, Map<String, Object> kieMap, Log log) {
        KieMetaInfoBuilder builder = new KieMetaInfoBuilder(kModule);
        KieModuleMetaInfo modelMetaInfo = builder.getKieModuleMetaInfo();

        /*Standard for the kieMap keys -> compilationID + dot + class name */
        StringBuilder sbModelMetaInfo = new StringBuilder(compilationID).append(".").append(KieModuleMetaInfo.class.getName());
        StringBuilder sbkModule = new StringBuilder(compilationID).append(".").append(FileKieModule.class.getName());

        if (modelMetaInfo != null) {
            kieMap.put(sbModelMetaInfo.toString(), modelMetaInfo);
            log.info("KieModelMetaInfo available in the map shared with the Maven Embedder with key:" +sbModelMetaInfo.toString());
        }
        if (kModule != null) {
            kieMap.put(sbkModule.toString(), kModule);
            log.info("KieModule available in the map shared with the Maven Embedder with key:"+sbkModule.toString());
        }
    }

    public void shareStoreWithMap(ClassLoader classLoader, String compilationID, Map<String, Object> kieMap, Log log) {
        if (classLoader instanceof ProjectClassLoader) {
            ProjectClassLoader projectClassloder = (ProjectClassLoader) classLoader;
            Map<String, byte[]> types = projectClassloder.getStore();
            if (projectClassloder.getStore() != null) {
                StringBuilder sbTypes = new StringBuilder(compilationID).append(".").append("ProjectClassloaderStore");
                kieMap.put(sbTypes.toString(), types);
                log.info("ProjectClassloader Store available in the map shared with the Maven Embedder");
            }
        }
    }

    public void shareTypesMetaInfoWithMap(InternalKieModule kModule, String compilationID, Map<String, Object> kieMap, Log log) {
        KieMetaInfoBuilder kb = new KieMetaInfoBuilder(kModule);
        KieModuleMetaInfo info = kb.generateKieModuleMetaInfo(null);
        Map<String, TypeMetaInfo> typesMetaInfos = info.getTypeMetaInfos();

        if (typesMetaInfos != null) {
            StringBuilder sbTypes = new StringBuilder(compilationID).append(".").append(TypeMetaInfo.class.getName());
            Set<String> eventClasses = new HashSet<>();
            for (Map.Entry<String, TypeMetaInfo> item : typesMetaInfos.entrySet()) {
                if (item.getValue().isEvent()) {
                    eventClasses.add(item.getKey());
                }
            }
            kieMap.put(sbTypes.toString(), eventClasses);
            log.info("TypesMetaInfo keys available in the map shared with the Maven Embedder");
        }
    }

}
