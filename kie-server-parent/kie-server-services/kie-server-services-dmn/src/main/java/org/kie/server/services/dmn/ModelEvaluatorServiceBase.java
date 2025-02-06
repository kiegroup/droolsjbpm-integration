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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieRuntimeFactory;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.api.core.ast.DecisionNode;
import org.kie.dmn.api.core.ast.DecisionServiceNode;
import org.kie.dmn.api.core.ast.InputDataNode;
import org.kie.dmn.api.core.event.DMNRuntimeEventListener;
import org.kie.dmn.backend.marshalling.v1x.DMNMarshallerFactory;
import org.kie.dmn.core.ast.InputDataNodeImpl;
import org.kie.dmn.core.ast.ItemDefNodeImpl;
import org.kie.dmn.core.internal.utils.DMNEvaluationUtils;
import org.kie.dmn.core.internal.utils.DMNEvaluationUtils.DMNEvaluationResult;
import org.kie.dmn.core.internal.utils.DynamicDMNContextBuilder;
import org.kie.dmn.model.api.BusinessKnowledgeModel;
import org.kie.dmn.model.api.DRGElement;
import org.kie.dmn.model.api.Decision;
import org.kie.dmn.model.api.Definitions;
import org.kie.dmn.model.api.InputData;
import org.kie.dmn.model.api.ItemDefinition;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.dmn.DMNContextKS;
import org.kie.server.api.model.dmn.DMNDecisionInfo;
import org.kie.server.api.model.dmn.DMNDecisionServiceInfo;
import org.kie.server.api.model.dmn.DMNInputDataInfo;
import org.kie.server.api.model.dmn.DMNItemDefinitionInfo;
import org.kie.server.api.model.dmn.DMNModelInfo;
import org.kie.server.api.model.dmn.DMNModelInfoList;
import org.kie.server.api.model.dmn.DMNQNameInfo;
import org.kie.server.api.model.dmn.DMNResultKS;
import org.kie.server.api.model.dmn.DMNUnaryTestsInfo;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.dmn.modelspecific.DMNFEELComparablePeriodSerializer;
import org.kie.server.services.dmn.modelspecific.KogitoDMNResult;
import org.kie.server.services.dmn.modelspecific.MSConsts;
import org.kie.server.services.dmn.modelspecific.OASGenerator;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.locator.ContainerLocatorProvider;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.kie.server.services.prometheus.PrometheusKieServerExtension;
import org.kie.server.services.prometheus.PrometheusMetricsDMNListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelEvaluatorServiceBase {

    private static final Logger LOG = LoggerFactory.getLogger( ModelEvaluatorServiceBase.class );

    private KieServerRegistry context;
    private MarshallerHelper marshallerHelper;
    
    private static final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .registerModule(new com.fasterxml.jackson.databind.module.SimpleModule()
                            .addSerializer(org.kie.dmn.feel.lang.types.impl.ComparablePeriod.class,
                                           new DMNFEELComparablePeriodSerializer()))
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            .configure(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES, false)
            .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);

    public ModelEvaluatorServiceBase(KieServerRegistry context) {
        this.context = context;
        this.marshallerHelper = new MarshallerHelper(context);
    }
    
    public ServiceResponse<DMNModelInfoList> getModels(String containerId) {
        try {
            KieContainerInstanceImpl kContainer = context.getContainer(containerId, ContainerLocatorProvider.get().getLocator());
            DMNRuntime kieRuntime = KieRuntimeFactory.of(kContainer.getKieContainer().getKieBase()).get(DMNRuntime.class);
            
            List<DMNModel> models = kieRuntime.getModels();
            List<DMNModelInfo> result = models.stream().map(ModelEvaluatorServiceBase::modelToInfo).collect(Collectors.toList());
            
            return new ServiceResponse<DMNModelInfoList>(
                    ServiceResponse.ResponseType.SUCCESS,
                    "OK models successfully retrieved from container '" + containerId + "'",
                    new DMNModelInfoList( result ) );
        } catch ( Exception e ) {
            LOG.error( "Error retrieving models from container '" + containerId + "'", e );
            return new ServiceResponse<DMNModelInfoList>(
                    ServiceResponse.ResponseType.FAILURE,
                    "Error retrieving models from container '" + containerId + "'" + e.getMessage(),
                    null );
        }
    }
    
    public static DMNModelInfo modelToInfo(DMNModel model) {
        DMNModelInfo res = new DMNModelInfo();
        res.setNamespace(model.getNamespace());
        res.setName(model.getName());
        res.setId(model.getDefinitions().getId());
        res.setDecisions(model.getDecisions().stream().map(ModelEvaluatorServiceBase::decisionToInfo).collect(Collectors.toSet()));
        res.setDecisionServices(model.getDecisionServices().stream().map(ModelEvaluatorServiceBase::decisionServiceToInfo).collect(Collectors.toSet()));
        res.setInputs(model.getInputs().stream().map(ModelEvaluatorServiceBase::inputDataToInfo).collect(Collectors.toSet()));
        res.setItemDefinitions(model.getItemDefinitions().stream().map(id -> itemDefinitionToInfo(((ItemDefNodeImpl) id).getItemDef())).collect(Collectors.toSet()));
        return res;
    }
    
    public static DMNDecisionServiceInfo decisionServiceToInfo(DecisionServiceNode dsNode) {
        DMNDecisionServiceInfo res = new DMNDecisionServiceInfo();
        res.setName(dsNode.getName());
        res.setId(dsNode.getId());
        return res;
    }

    public static DMNDecisionInfo decisionToInfo(DecisionNode decisionNode) {
        DMNDecisionInfo res = new DMNDecisionInfo();
        res.setName(decisionNode.getName());
        res.setId(decisionNode.getId());
        return res;
    }
    
    public static DMNInputDataInfo inputDataToInfo(InputDataNode inputDataNode) {
        DMNInputDataInfo res = new DMNInputDataInfo();
        res.setName(inputDataNode.getName());
        res.setId(inputDataNode.getId());
        InputData id = ((InputDataNodeImpl) inputDataNode).getInputData();
        QName typeRef = id.getVariable().getTypeRef();
        // for InputData sometimes the NS is not really valorized inside the jdk QName as internally ns are resolved by prefix directly.
        if (typeRef != null) {
            if (XMLConstants.NULL_NS_URI.equals(typeRef.getNamespaceURI())) {
                String actualNS = id.getNamespaceURI(typeRef.getPrefix());
                typeRef = new QName(actualNS, typeRef.getLocalPart(), typeRef.getPrefix());
            }
            res.setTypeRef(DMNQNameInfo.of(typeRef));
        }
        return res;
    }
    
    public static DMNItemDefinitionInfo itemDefinitionToInfo(ItemDefinition itemDef) {
        DMNItemDefinitionInfo res = new DMNItemDefinitionInfo();
        res.setId(itemDef.getId());
        res.setName(itemDef.getName());
        if (itemDef.getTypeRef() != null) {
            res.setTypeRef(DMNQNameInfo.of(itemDef.getTypeRef()));
        }
        if (itemDef.getAllowedValues() != null) {
            DMNUnaryTestsInfo av = new DMNUnaryTestsInfo();
            av.setText(itemDef.getAllowedValues().getText());
            av.setExpressionLanguage(itemDef.getAllowedValues().getExpressionLanguage());
            res.setAllowedValues(av);
        }
        if (itemDef.getItemComponent() != null && !itemDef.getItemComponent().isEmpty()) {
            List<DMNItemDefinitionInfo> components = itemDef.getItemComponent().stream().map(ModelEvaluatorServiceBase::itemDefinitionToInfo).collect(Collectors.toList());
            res.setItemComponent(components);
        }
        res.setTypeLanguage(itemDef.getTypeLanguage());
        res.setIsCollection(itemDef.isIsCollection());
        return res;
    }


    public ServiceResponse<DMNResultKS> evaluateDecisions(String containerId, String contextPayload, String marshallingType) {
        try {
            KieContainerInstanceImpl kContainer = context.getContainer(containerId, ContainerLocatorProvider.get().getLocator());
            DMNRuntime dmnRuntime = KieRuntimeFactory.of(kContainer.getKieContainer().getKieBase()).get(DMNRuntime.class);

            wirePrometheus(kContainer, dmnRuntime);

            LOG.debug("Will deserialize payload: {}", contextPayload);
            DMNContextKS evalCtx = marshallerHelper.unmarshal(containerId, contextPayload, marshallingType, DMNContextKS.class);
            
            DMNEvaluationResult evaluationResult = DMNEvaluationUtils.evaluate(dmnRuntime,
                                                                               evalCtx.getNamespace(),
                                                                               evalCtx.getModelName(),
                                                                               evalCtx.getDmnContext(),
                                                                               evalCtx.getDecisionNames(),
                                                                               evalCtx.getDecisionIds(),
                                                                               evalCtx.getDecisionServiceName());

            DMNResultKS res = new DMNResultKS(evaluationResult.model.getNamespace(),
                                              evaluationResult.model.getName(),
                                              evalCtx.getDecisionNames(),
                                              evaluationResult.result);
            
            return new ServiceResponse<DMNResultKS>(
                    ServiceResponse.ResponseType.SUCCESS,
                    "OK from container '" + containerId + "'",
                    res );
        } catch ( Exception e ) {
            e.printStackTrace();
            LOG.error( "Error from container '" + containerId + "'", e );
            return new ServiceResponse<DMNResultKS>(
                    ServiceResponse.ResponseType.FAILURE,
                    "Error from container '" + containerId + "'" + e.getMessage(),
                    null );
        }
    }

    public Response evaluateModel(String containerId, String modelId, String contextPayload, boolean asDmnResult, String decisionServiceId) {
        try {
            KieContainerInstanceImpl kContainer = context.getContainer(containerId, ContainerLocatorProvider.get().getLocator());
            DMNRuntime dmnRuntime = KieRuntimeFactory.of(kContainer.getKieContainer().getKieBase()).get(DMNRuntime.class);

            List<DMNModel> modelsWithID = dmnRuntime.getModels().stream().filter(m -> m.getName().equals(modelId)).collect(Collectors.toList());
            if (modelsWithID.isEmpty()) {
                return Response.status(Status.NOT_FOUND).entity("No model identifies with modelId: " + modelId).build();
            } else if (modelsWithID.size() > 1) {
                return Response.status(Status.NOT_FOUND).entity("More than one existing DMN model having modelId: " + modelId).build();
            }
            DMNModel dmnModel = modelsWithID.get(0);
            DecisionServiceNode determinedDS = null;
            if (decisionServiceId != null) {
                Optional<DecisionServiceNode> dsOpt = dmnModel.getDecisionServices().stream().filter(ds -> ds.getName().equals(decisionServiceId)).findFirst();
                if (!dsOpt.isPresent()) {
                    return Response.status(Status.NOT_FOUND).entity("No decisionService found: " + decisionServiceId).build();
                }
                determinedDS = dsOpt.get();
            }

            Map<String, Object> jsonContextMap = objectMapper.readValue(contextPayload, new TypeReference<Map<String, Object>>() {});
            DMNContext dmnContext = new DynamicDMNContextBuilder(dmnRuntime.newContext(), dmnModel).populateContextWith(jsonContextMap);

            wirePrometheus(kContainer, dmnRuntime);

            DMNResult determinedResult = null;
            if (determinedDS != null) {
                determinedResult = dmnRuntime.evaluateDecisionService(dmnModel, dmnContext, determinedDS.getName());
            } else {
                determinedResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);
            }

            // at this point the DMN service has executed the evaluation, so it's full model-specific endpoint semantics.
            KogitoDMNResult result = new KogitoDMNResult(dmnModel.getNamespace(), dmnModel.getName(), determinedResult);
            if (asDmnResult) {
                return Response.ok().entity(objectMapper.writeValueAsString(result)).build();
            }
            String responseJSON = null;
            if (determinedDS != null && determinedDS.getDecisionService().getOutputDecision().size() == 1) {
                responseJSON = objectMapper.writeValueAsString(result.getDecisionResults().get(0).getResult());
            } else {
                responseJSON = objectMapper.writeValueAsString(result.getDmnContext());
            }
            ResponseBuilder response = Response.ok();
            if (result.hasErrors()) {
                String infoWarns = result.getMessages().stream().map(m -> m.getLevel() + " " + m.getMessage()).collect(java.util.stream.Collectors.joining(", "));
                response.header(MSConsts.KOGITO_DECISION_INFOWARN_HEADER, infoWarns);
            }
            response.entity(responseJSON);
            return response.build();
        } catch (Exception e) {
            LOG.error("Error from container '" + containerId + "'", e);
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    private void wirePrometheus(KieContainerInstanceImpl kContainer, DMNRuntime dmnRuntime) {
        PrometheusKieServerExtension extension = (PrometheusKieServerExtension) context.getServerExtension(PrometheusKieServerExtension.EXTENSION_NAME);
        if (extension != null) {
            //default handler
            PrometheusMetricsDMNListener listener = new PrometheusMetricsDMNListener(PrometheusKieServerExtension.getMetrics(), kContainer);
            dmnRuntime.addListener(listener);

            //custom handler
            List<DMNRuntimeEventListener> listeners = extension.getDMNRuntimeListeners(kContainer);
            listeners.forEach(l -> {
                if (!dmnRuntime.getListeners().contains(l)) {
                    dmnRuntime.addListener(l);
                }
            });

        }
    }

    public KieServerRegistry getKieServerRegistry() {
        return this.context;
    }

    public Response getModel(String containerId, String modelId) {
        try {
            KieContainerInstanceImpl kContainer = context.getContainer(containerId, ContainerLocatorProvider.get().getLocator());
            DMNRuntime dmnRuntime = KieRuntimeFactory.of(kContainer.getKieContainer().getKieBase()).get(DMNRuntime.class);

            List<DMNModel> modelsWithID = dmnRuntime.getModels().stream().filter(m -> m.getName().equals(modelId)).collect(Collectors.toList());
            if (modelsWithID.isEmpty()) {
                return Response.status(Status.NOT_FOUND).entity("No model identifies with modelId: " + modelId).build();
            } else if (modelsWithID.size() > 1) {
                return Response.status(Status.NOT_FOUND).entity("More than one existing DMN model having modelId: " + modelId).build();
            }
            DMNModel dmnModel = modelsWithID.get(0);
            Definitions definitions = dmnModel.getDefinitions();

            for (DRGElement drg : definitions.getDrgElement()) {
                if (drg instanceof Decision) {
                    Decision decision = (Decision) drg;
                    decision.setExpression(null);
                } else if (drg instanceof BusinessKnowledgeModel) {
                    BusinessKnowledgeModel bkm = (BusinessKnowledgeModel) drg;
                    bkm.setEncapsulatedLogic(null);
                }
            }
            
            String xml = DMNMarshallerFactory.newDefaultMarshaller().marshal(definitions);
            return Response.ok().entity(xml).build();
        } catch (Exception e) {
            LOG.error("Error from container '" + containerId + "'", e);
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
    
    public Response getOAS(String containerId, boolean asJSON) {
        try {
            KieContainerInstanceImpl kContainer = context.getContainer(containerId, ContainerLocatorProvider.get().getLocator());
            ReleaseId resolvedReleaseId = kContainer.getKieContainer().getResolvedReleaseId();
            DMNRuntime dmnRuntime = KieRuntimeFactory.of(kContainer.getKieContainer().getKieBase()).get(DMNRuntime.class);
            Collection<DMNModel> models = dmnRuntime.getModels();
            String content = new OASGenerator(containerId, resolvedReleaseId).generateOAS(models, asJSON);
            return Response.ok().entity(content).build();
        } catch (Exception e) {
            LOG.error("Error from container '" + containerId + "'", e);
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}
