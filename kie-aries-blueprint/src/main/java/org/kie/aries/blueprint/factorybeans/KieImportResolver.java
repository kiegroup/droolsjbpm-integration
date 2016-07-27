/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.kie.aries.blueprint.factorybeans;


import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.aries.blueprint.namespace.BlueprintContextHelper;

public class KieImportResolver extends AbstractKieObjectsResolver {

    private final String releaseIdName;
    private final boolean scannerEnabled;
    private final long scannerInterval;

    private KieContainer kieContainer;

    public KieImportResolver( String releaseIdName, ReleaseId releaseId, boolean scannerEnabled, long scannerInterval ) {
        super( releaseId );
        this.releaseIdName = releaseIdName;
        this.scannerEnabled = scannerEnabled;
        this.scannerInterval = scannerInterval;
    }

    @Override
    public Object init(BlueprintContextHelper context) {
        KieContainer kContainer = registerKieContainer(context);
        registerKieBases(context, kContainer);
        return kContainer;
    }

    private synchronized KieContainer registerKieContainer(BlueprintContextHelper context) {
        if (kieContainer == null) {
            KieServices ks = KieServices.Factory.get();
            if ( releaseId == null ) {
                kieContainer = ks.getKieClasspathContainer();
            } else {
                kieContainer = resolveKContainer( releaseId );
                if (scannerEnabled) {
                    KieScanner kieScanner = KieServices.Factory.get().newKieScanner( kieContainer );
                    context.registerBean(releaseIdName+"#scanner", kieScanner);
                    if (scannerInterval > 0) {
                        kieScanner.start( scannerInterval );
                    }
                }
            }
        }
        return kieContainer;
    }

    private void registerKieBases(BlueprintContextHelper context, KieContainer kContainer) {
        for (String kieBaseName : kContainer.getKieBaseNames()) {
            KieBase kieBase = kContainer.getKieBase( kieBaseName );
            context.registerBean(kieBaseName, kieBase);
            registerKieSessions(context, kieBaseName, kContainer);
        }
    }

    private void registerKieSessions(BlueprintContextHelper context, String kieBaseName, KieContainer kContainer) {
        for (String kieSessionName : kContainer.getKieSessionNamesInKieBase(kieBaseName)) {
            Object ksession = resolveKSession(kieSessionName, kContainer);
            context.registerBean(kieSessionName, ksession);
        }
    }
}
