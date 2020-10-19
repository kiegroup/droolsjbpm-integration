/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.impl.util;

import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.drools.compiler.kie.builder.impl.KieProject;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.runtime.CommandExecutor;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.services.impl.KieContainerInstanceImpl;

public class KieServerUtils {

    private static final String SNAPSHOT = "-SNAPSHOT";

    public static boolean isSnapshot(final ReleaseId releaseId) {
        return isSnapshot(releaseId.getVersion());
    }

    public static boolean isSnapshot(final org.kie.api.builder.ReleaseId releaseId) {
        return isSnapshot(releaseId.getVersion());
    }

    public static boolean isSnapshot(final String version) {
        return version.toUpperCase().endsWith(SNAPSHOT);
    }

    public static CommandExecutor getDefaultKieSession(KieContainerInstanceImpl kci) {
        KieProject kieProject = ((KieContainerImpl) kci.getKieContainer()).getKieProject();
        KieSessionModel defaultStatefulModel = kieProject.getDefaultKieSession();
        KieSessionModel defaultStatelessModel = kieProject.getDefaultStatelessKieSession();
        // If both stateful and statelss default ksession exist, stateful is used.
        if (defaultStatefulModel != null) {
            return kci.getKieContainer().getKieSession();
        } else if (defaultStatelessModel != null) {
            return kci.getKieContainer().getStatelessKieSession();
        } else {
            throw new IllegalStateException("No default KieSession found on container '" + kci.getContainerId() + "'.");
        }
    }
}
