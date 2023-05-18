/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.dmn.modelspecific;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map.Entry;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.kie.api.builder.ReleaseId;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNType;
import org.kie.dmn.model.api.DecisionService;
import org.kie.dmn.openapi.DMNOASGeneratorFactory;
import org.kie.dmn.openapi.model.DMNModelIOSets;
import org.kie.dmn.openapi.model.DMNOASResult;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;

import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.OpenApiStaticFile;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;

public class OASGenerator {

    private final String containerId;
    private final ReleaseId releaseId;

    public OASGenerator(String containerId, ReleaseId releaseId) {
        this.containerId = containerId;
        this.releaseId = releaseId;
    }

    public String generateOAS(Collection<DMNModel> models, boolean asJSON) throws Exception, IOException {
        DMNOASResult dmnoas = DMNOASGeneratorFactory.generator(models, "#/components/schemas/").build();
        OpenAPI openAPI = loadOASTemplate();
        Info info = OASFactory.createObject(Info.class)
                              .title(containerId + " DMN endpoints")
                              .description("DMN model-specific OAS generated for container " + containerId + " as " + releaseId)
                              .version(releaseId.getVersion());
        openAPI.info(info);
        String contextRoot = KieServerEnvironment.getContextRoot();
        String sbRoot = System.getProperty(KieServerConstants.CFG_SB_CXF_PATH);
        if (contextRoot != null) {
            openAPI.addServer(OASFactory.createObject(Server.class).url(contextRoot + "/services/rest"));
        } else if (sbRoot != null) {
            openAPI.addServer(OASFactory.createObject(Server.class).url(sbRoot));
        }
        for (Entry<DMNType, Schema> kv : dmnoas.getSchemas().entrySet()) {
            openAPI.getComponents().addSchema(dmnoas.getNamingPolicy().getName(kv.getKey()), kv.getValue());
        }
        openAPI.paths(OASFactory.createObject(Paths.class));
        for (DMNModel dmnModel : models) {
            openAPI.addTag(OASFactory.createTag().name(dmnModel.getName()).description(" "));
            pathModel(openAPI, dmnModel, dmnoas);
            pathModelDMNResult(openAPI, dmnModel, dmnoas);
            for (DecisionService ds : dmnModel.getDefinitions().getDecisionService()) {
                if (ds.getAdditionalAttributes().keySet().stream().anyMatch(qn -> qn.getLocalPart().equals("dynamicDecisionService"))) {
                    continue;
                }
                pathModelDS(openAPI, dmnModel, ds, dmnoas);
                pathModelDSDMNResult(openAPI, dmnModel, ds, dmnoas);
            }
        }
        String content = OpenApiSerializer.serialize(openAPI, asJSON ? Format.JSON : Format.YAML);
        return content;
    }

    private void pathModelDSDMNResult(OpenAPI openAPI, DMNModel dmnModel, DecisionService ds, DMNOASResult dmnoas) {
        final String description = "model decision service '" + ds.getName() + "' for structured DMNResult";
        DMNModelIOSets ioSets = dmnoas.lookupIOSetsByModel(dmnModel);
        DMNType identifyInputSet = ioSets.lookupDSIOSetsByName(ds.getName()).getDSInputSet();
        String inputRef = dmnoas.getNamingPolicy().getRef(identifyInputSet);
        Operation operation = buildOperationWithIORefs(description, inputRef, "#/components/schemas/" + MSConsts.MSDMNE_KOGITO_DMN_RESULT).summary(description).addTag(dmnModel.getName());
        PathItem pathItem = OASFactory.createObject(PathItem.class);
        pathItem.POST(operation);
        openAPI.getPaths().addPathItem("/server/containers/" + containerId + "/dmn/models/" + dmnModel.getName() + "/" + ds.getName() + "/dmnresult", pathItem);
    }

    private void pathModelDS(OpenAPI openAPI, DMNModel dmnModel, DecisionService ds, DMNOASResult dmnoas) {
        final String description = "model decision service '" + ds.getName() + "'";
        DMNModelIOSets ioSets = dmnoas.lookupIOSetsByModel(dmnModel);
        DMNType identifyInputSet = ioSets.lookupDSIOSetsByName(ds.getName()).getDSInputSet();
        DMNType identifyOutputSet = ioSets.lookupDSIOSetsByName(ds.getName()).getDSOutputSet();
        String inputRef = dmnoas.getNamingPolicy().getRef(identifyInputSet);
        String outputRef = dmnoas.getNamingPolicy().getRef(identifyOutputSet);
        Operation operation = buildOperationWithIORefs(description, inputRef, outputRef).summary(description).addTag(dmnModel.getName());
        PathItem pathItem = OASFactory.createObject(PathItem.class);
        pathItem.POST(operation);
        openAPI.getPaths().addPathItem("/server/containers/" + containerId + "/dmn/models/" + dmnModel.getName() + "/" + ds.getName(), pathItem);
    }

    private void pathModelDMNResult(OpenAPI openAPI, DMNModel dmnModel, DMNOASResult dmnoas) {
        final String description = "model evaluation for structured DMNResult";
        DMNModelIOSets ioSets = dmnoas.lookupIOSetsByModel(dmnModel);
        DMNType identifyInputSet = ioSets.getInputSet();
        String inputRef = dmnoas.getNamingPolicy().getRef(identifyInputSet);
        Operation operation = buildOperationWithIORefs(description, inputRef, "#/components/schemas/" + MSConsts.MSDMNE_KOGITO_DMN_RESULT).summary(description).addTag(dmnModel.getName());
        PathItem pathItem = OASFactory.createObject(PathItem.class);
        pathItem.POST(operation);
        openAPI.getPaths().addPathItem("/server/containers/" + containerId + "/dmn/models/" + dmnModel.getName() + "/dmnresult", pathItem);
    }

    private void pathModel(OpenAPI openAPI, DMNModel dmnModel, DMNOASResult dmnoas) {
        PathItem pathItem = OASFactory.createObject(PathItem.class);
        openAPI.getPaths().addPathItem("/server/containers/" + containerId + "/dmn/models/" + dmnModel.getName(), pathItem);
        pathModelGET(pathItem, dmnModel);
        pathModelPOST(pathItem, dmnModel, dmnoas);
    }

    private void pathModelPOST(PathItem pathItem, DMNModel dmnModel, DMNOASResult dmnoas) {
        final String description = "model evaluation";
        DMNModelIOSets ioSets = dmnoas.lookupIOSetsByModel(dmnModel);
        DMNType identifyInputSet = ioSets.getInputSet();
        DMNType identifyOutputSet = ioSets.getOutputSet();
        String inputRef = dmnoas.getNamingPolicy().getRef(identifyInputSet);
        String outputRef = dmnoas.getNamingPolicy().getRef(identifyOutputSet);
        Operation operation = buildOperationWithIORefs(description, inputRef, outputRef).summary(description).addTag(dmnModel.getName());
        pathItem.POST(operation);
    }

    private void pathModelGET(PathItem pathItem, DMNModel dmnModel) {
        MediaType mediaType = OASFactory.createMediaType().schema(OASFactory.createSchema().type(SchemaType.STRING));
        Content content = OASFactory.createObject(Content.class).addMediaType(javax.ws.rs.core.MediaType.APPLICATION_XML, mediaType);
        APIResponse apiResponse = OASFactory.createObject(APIResponse.class).description("model without decision-logic");
        apiResponse.content(content);
        APIResponses apiResponses = OASFactory.createObject(APIResponses.class);
        apiResponses.defaultValue(apiResponse);
        Operation getOperation = OASFactory.createObject(Operation.class).responses(apiResponses).summary("Retrieve DMN model XML without decision-logic").addTag(dmnModel.getName());
        pathItem.GET(getOperation);
    }

    private Operation buildOperationWithIORefs(String description, String inputRef, String outputRef) {
        MediaType mediaType = OASFactory.createMediaType().schema(OASFactory.createSchema().ref(outputRef));
        Content content = OASFactory.createObject(Content.class).addMediaType(javax.ws.rs.core.MediaType.APPLICATION_JSON, mediaType);
        APIResponse apiResponse = OASFactory.createObject(APIResponse.class).description(description);
        apiResponse.content(content);
        APIResponses apiResponses = OASFactory.createObject(APIResponses.class);
        apiResponses.defaultValue(apiResponse);
        MediaType requestMediaType = OASFactory.createMediaType().schema(OASFactory.createSchema().ref(inputRef));
        Content requestContent = OASFactory.createContent().addMediaType(javax.ws.rs.core.MediaType.APPLICATION_JSON, requestMediaType);
        RequestBody requestBody = OASFactory.createRequestBody().description(description).content(requestContent);
        Operation operation = OASFactory.createObject(Operation.class).responses(apiResponses).requestBody(requestBody);
        return operation;
    }

    private OpenAPI loadOASTemplate() throws Exception {
        try (InputStream is = this.getClass().getResourceAsStream("/oasTemplate.json");
                OpenApiStaticFile openApiStaticFile = new OpenApiStaticFile(is, Format.JSON)) {
            return OpenApiProcessor.modelFromStaticFile(openApiStaticFile);
        }
    }

}
