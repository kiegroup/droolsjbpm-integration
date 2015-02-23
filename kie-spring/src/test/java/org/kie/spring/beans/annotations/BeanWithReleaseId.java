/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.spring.beans.annotations;

import org.kie.api.KieBase;
import org.kie.api.cdi.KBase;
import org.kie.api.cdi.KContainer;
import org.kie.api.cdi.KReleaseId;
import org.kie.api.cdi.KSession;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.spring.CodeVersion;

public class BeanWithReleaseId {

    @KReleaseId(groupId = "org.drools", artifactId = "named-kiesession", version = CodeVersion.VERSION)
    @KBase("kbase1")
    KieBase kieBase;

    public KieBase getKieBase() {
        return kieBase;
    }
    public void setKieBase(KieBase kieBase) {
        this.kieBase = kieBase;
    }

    @KContainer
    @KReleaseId(groupId = "org.drools", artifactId = "named-kiesession", version = CodeVersion.VERSION)
    KieContainer kieContainer;

    public KieContainer getKieContainer() {
        return kieContainer;
    }
    public void setKieContainer(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    @KSession("ksession1")
    @KReleaseId(groupId = "org.drools", artifactId = "named-kiesession", version = CodeVersion.VERSION)
    KieSession kieSession;

    public KieSession getKieSession() {
        return kieSession;
    }

    public void setKieSession(KieSession kieSession) {
        this.kieSession = kieSession;
    }
}
