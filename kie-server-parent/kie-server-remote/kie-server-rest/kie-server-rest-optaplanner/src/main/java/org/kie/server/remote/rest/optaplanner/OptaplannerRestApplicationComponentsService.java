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

package org.kie.server.remote.rest.optaplanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.optaplanner.OptaplannerKieServerExtension;
import org.kie.server.services.optaplanner.SolverServiceBase;

public class OptaplannerRestApplicationComponentsService
        implements KieServerApplicationComponentsService {

    private static final String OWNER_EXTENSION = OptaplannerKieServerExtension.EXTENSION_NAME;

    @Override
    public Collection<Object> getAppComponents(String extension, SupportedTransports type, Object... services) {
        // skip calls from other than owning extension
        if ( !OWNER_EXTENSION.equals( extension ) ) {
            return Collections.emptyList();
        }

        SolverServiceBase solverServiceBase = null;
        KieServerRegistry context = null;

        for ( Object object : services ) {
            // in case given service is null (meaning was not configured) continue with next one
            if ( object == null ) {
                continue;
            }
            if ( SolverServiceBase.class.isAssignableFrom( object.getClass() ) ) {
                solverServiceBase = (SolverServiceBase) object;
                continue;
            } else if ( KieServerRegistry.class.isAssignableFrom( object.getClass() ) ) {
                context = (KieServerRegistry) object;
                continue;
            }
        }

        List<Object> components = new ArrayList<Object>( 1 );
        components.add( new SolverResource( solverServiceBase ) );

        return components;
    }

}
