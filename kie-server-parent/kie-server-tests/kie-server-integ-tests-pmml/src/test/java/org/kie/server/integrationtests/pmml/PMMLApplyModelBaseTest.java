package org.kie.server.integrationtests.pmml;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.drools.core.command.runtime.pmml.ApplyPmmlModelCommand;
import org.kie.api.KieServices;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;

public abstract class PMMLApplyModelBaseTest extends RestJmsSharedBaseIntegrationTest {

    protected RuleServicesClient ruleClient;

    public static void setup(String resourceDir,
                             long extendedTimeout,
                             String containerId,
                             ReleaseId releaseId) {
        commandsFactory = KieServices.Factory.get().getCommands();
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource(resourceDir);

        // Having timeout issues due to pmml -> raised timeout.
        KieServicesClient client = createDefaultStaticClient(extendedTimeout);
        ServiceResponse<KieContainerResource> reply = client.createContainer(containerId, new KieContainerResource(containerId, releaseId));
        KieServerAssert.assertSuccess(reply);
    }

    protected void execute(String correlationId,
                           String containerId,
                           String modelName,
                           String fileName,
                           String targetField,
                           Object expectedResult,
                           Map<String, Object> inputData) {

        final PMMLRequestData request = new PMMLRequestData(correlationId, modelName);
        request.setSource(fileName);
        inputData.forEach(request::addRequestParam);

        final ApplyPmmlModelCommand command = (ApplyPmmlModelCommand) commandsFactory.newApplyPmmlModel(request);
        final ServiceResponse<ExecutionResults> results = ruleClient.executeCommandsWithResults(containerId, command);
        Assertions.assertThat(results.getResult()).isNotNull();
        Assertions.assertThat(results.getResult().getValue("results")).isNotNull();


        final PMML4Result resultHolder = (PMML4Result) results.getResult().getValue("results");
        Assertions.assertThat(resultHolder).isNotNull();
        Assertions.assertThat(resultHolder.getResultCode()).isEqualTo("OK");

        String resultObjectName = resultHolder.getResultObjectName();
        Assertions.assertThat(resultObjectName).isNotNull().isEqualTo(targetField);

        final Object obj = resultHolder.getResultValue(targetField, null);
        Assertions.assertThat(obj).isNotNull().isEqualTo(expectedResult);
    }

    @Override
    protected void setupClients(KieServicesClient kieServicesClient) {
        this.ruleClient = kieServicesClient.getServicesClient(RuleServicesClient.class);
    }
}
