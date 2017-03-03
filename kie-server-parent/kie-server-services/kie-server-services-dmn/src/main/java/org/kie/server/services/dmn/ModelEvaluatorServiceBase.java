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

import org.kie.api.runtime.KieSession;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;   
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.server.api.model.*;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.dmn.DMNContextKS;
import org.kie.server.api.model.dmn.DMNResultKS;
import org.kie.server.api.model.instance.ScoreWrapper;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.instance.SolverInstanceList;
import org.kie.server.api.model.type.JaxbList;
import org.kie.server.api.model.type.JaxbMap;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class ModelEvaluatorServiceBase {

    private static final Logger LOG = LoggerFactory.getLogger( ModelEvaluatorServiceBase.class );

    private KieServerRegistry context;
    private MarshallerHelper marshallerHelper;

    public ModelEvaluatorServiceBase(KieServerRegistry context) {
        this.context = context;
        this.marshallerHelper = new MarshallerHelper(context);
    }
    
    public ServiceResponse<JaxbList> getModels(String containerId) {
        try {
            KieContainerInstanceImpl kContainer = context.getContainer(containerId);
            KieSession kieSession = kContainer.getKieContainer().newKieSession();
            DMNRuntime kieRuntime = kieSession.getKieRuntime(DMNRuntime.class);
            List result = kieRuntime.getModels();
            return new ServiceResponse<JaxbList>(
                    ServiceResponse.ResponseType.SUCCESS,
                    "OK models successfully retrieved from container '" + containerId + "'",
                    new JaxbList( result ) );
        } catch ( Exception e ) {
            LOG.error( "Error retrieving models from container '" + containerId + "'", e );
            return new ServiceResponse<JaxbList>(
                    ServiceResponse.ResponseType.FAILURE,
                    "Error retrieving models from container '" + containerId + "'" + e.getMessage(),
                    null );
        }
    }
    
    public ServiceResponse<DMNResultKS> evaluateAllDecisions(String containerId, String contextPayload, String marshallingType) {
        try {
            KieContainerInstanceImpl kContainer = context.getContainer(containerId);
            KieSession kieSession = kContainer.getKieContainer().newKieSession();
            DMNRuntime dmnRuntime = kieSession.getKieRuntime(DMNRuntime.class);
            
            LOG.info("Will deserialize payload: {}", contextPayload);
            DMNContextKS evalCtx = marshallerHelper.unmarshal(containerId, contextPayload, marshallingType, DMNContextKS.class);
            
            DMNModel model;
            if ( evalCtx.getModelName() == null ) {
                if ( dmnRuntime.getModels().size() > 1 ) {
                    throw new RuntimeException("more than one (default) model");
                }
                
                model = dmnRuntime.getModels().get(0);
            } else {
                model = dmnRuntime.getModel(evalCtx.getNamespace(), evalCtx.getModelName());
            }
            if ( model == null ) {
                throw new RuntimeException("Unable to locate DMN Model to evaluate");
            }
            LOG.info("Will use model: {}", model);
            
            DMNContext dmnContext = DMNFactory.newContext();
            for ( Entry<String, Object> e : evalCtx.getDmnContext().entrySet() ) {
                dmnContext.set(e.getKey(), e.getValue());
            }
            LOG.info("Will use dmnContext: {}", dmnContext);
            
            DMNResult result = dmnRuntime.evaluateAll(model, dmnContext);
            
            LOG.info("Result:");
            LOG.info("{}",result);
            LOG.info("{}",result.getContext());
            LOG.info("{}",result.getDecisionResults());
            LOG.info("{}",result.getMessages());
            
            DMNResultKS res = new DMNResultKS(model.getNamespace(), model.getName(), evalCtx.getDecisionName(), result);
            
            kieSession.dispose();
            
            return new ServiceResponse<DMNResultKS>(
                    ServiceResponse.ResponseType.SUCCESS,
                    "OK from container '" + containerId + "'",
                    res );
        } catch ( Exception e ) {
            LOG.error( "Error from container '" + containerId + "'", e );
            return new ServiceResponse<DMNResultKS>(
                    ServiceResponse.ResponseType.FAILURE,
                    "Error from container '" + containerId + "'" + e.getMessage(),
                    null );
        }
    }

    public KieServerRegistry getKieServerRegistry() {
        return this.context;
    }

}
