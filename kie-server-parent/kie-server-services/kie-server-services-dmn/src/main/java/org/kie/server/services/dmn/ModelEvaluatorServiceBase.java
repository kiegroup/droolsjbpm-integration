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

package org.kie.server.services.dmn;

import org.kie.server.api.model.*;
import org.kie.server.api.model.instance.ScoreWrapper;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.instance.SolverInstanceList;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class ModelEvaluatorServiceBase {

    private static final Logger LOG = LoggerFactory.getLogger( ModelEvaluatorServiceBase.class );

    private KieServerRegistry context;

    public ModelEvaluatorServiceBase(KieServerRegistry context) {
        this.context = context;
    }

    public ServiceResponse<List<String>> getEvaluators(String containerId) {
        try {
            List<String> result = new ArrayList<>();
            result.add("abc");
            result.add("def");
            return new ServiceResponse<List<String>>(
                    ServiceResponse.ResponseType.SUCCESS,
                    "OK list successfully retrieved from container '" + containerId + "'",
                    result );
        } catch ( Exception e ) {
            LOG.error( "Error retrieving list from container '" + containerId + "'", e );
            return new ServiceResponse<List<String>>(
                    ServiceResponse.ResponseType.FAILURE,
                    "Error retrieving list from container '" + containerId + "'" + e.getMessage(),
                    null );
        }
    }

    public KieServerRegistry getKieServerRegistry() {
        return this.context;
    }

}
