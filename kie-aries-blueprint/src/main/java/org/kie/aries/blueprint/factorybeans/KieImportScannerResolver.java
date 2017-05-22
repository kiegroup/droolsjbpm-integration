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

import java.util.Collection;

import org.kie.api.builder.KieScanner;
import org.kie.api.event.kiescanner.KieScannerEventListener;
import org.kie.aries.blueprint.namespace.BlueprintContextHelper;

public class KieImportScannerResolver extends AbstractKieObjectsResolver implements KieScanner {

    private final String name;
    private KieScanner kieScanner;

    public KieImportScannerResolver( String name ) {
        super(null);
        this.name = name;
    }

    void setScanner( KieScanner kieScanner ) {
        this.kieScanner = kieScanner;
    }

    @Override
    public Object init( BlueprintContextHelper context ) {
        return this;
    }

    @Override
    public void start( long l ) {
        kieScanner.start( l );
    }

    @Override
    public void stop() {
        kieScanner.stop();
    }

    @Override
    public void shutdown() {
        kieScanner.shutdown();
    }

    @Override
    public void scanNow() {
        kieScanner.scanNow();
    }

    @Override
    public void addListener(KieScannerEventListener listener) {
        kieScanner.addListener(listener);
    }

    @Override
    public void removeListener(KieScannerEventListener listener) {
        kieScanner.removeListener(listener);        
    }

    @Override
    public Collection<KieScannerEventListener> getListeners() {
        return kieScanner.getListeners();
    }
}
