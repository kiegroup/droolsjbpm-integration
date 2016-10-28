/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.services.jbpm.ui.img;

import java.io.File;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.definition.process.*;
import org.kie.api.runtime.KieContainer;

public class ImageReference {

    private static final String SVG_SUFFIX = "-svg.svg";
    private static final String DEFAULT_KBASE_NAME = "defaultKieBase";

    private InternalKieModule kieModule;
    private KieContainer kieContainer;
    private String kieBaseName;

    public ImageReference(KieContainer kieContainer, String kieBaseName) {
        this.kieContainer = kieContainer;
        if (kieBaseName == null || kieBaseName.isEmpty()) {
            KieBaseModel defaultKBaseModel = ((KieContainerImpl)kieContainer).getKieProject().getDefaultKieBaseModel();
            if (defaultKBaseModel != null) {
                kieBaseName = defaultKBaseModel.getName();
            } else {
                kieBaseName = DEFAULT_KBASE_NAME;
            }
        }
        kieModule = (InternalKieModule) ((KieContainerImpl)kieContainer).getKieModuleForKBase(kieBaseName);
        this.kieBaseName = kieBaseName;
    }

    public byte[] getImageContent(String location, String name) {

        org.kie.api.definition.process.Process process = kieContainer.getKieBase(kieBaseName).getProcess(name);
        if (process != null) {
            String sourcePath = process.getResource().getSourcePath();
            if (sourcePath != null) {
                String processDirectory = new File(sourcePath).getParent();
                if (processDirectory == null) {
                    processDirectory = "";
                } else {
                    processDirectory += "/";
                }
                byte[] data = seek(processDirectory, name, kieModule);

                if (data != null) {
                    return data;
                }
                // set process directory as location in case the main search mechanism did not find the image
                location = processDirectory;
            }
        }

        byte[] data = seek(location, name, kieModule);

        if (data == null && kieModule.getKieDependencies() != null) {

            for (InternalKieModule depKieModule : kieModule.getKieDependencies().values()) {
                data = seek(location, name, depKieModule);
                if (data != null) {
                    break;
                }
            }
        }

        return data;
    }

    protected byte[] seek(String location, String name, InternalKieModule kieModule) {
        byte[] data = kieModule.getBytes(location + name + SVG_SUFFIX);

        if (data == null) {
            data = kieModule.getBytes(name + SVG_SUFFIX);
        }

        return data;
    }
}
