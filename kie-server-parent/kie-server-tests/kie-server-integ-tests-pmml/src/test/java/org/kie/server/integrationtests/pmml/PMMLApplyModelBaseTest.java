package org.kie.server.integrationtests.pmml;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.drools.core.command.runtime.pmml.ApplyPmmlModelCommand;
import org.junit.BeforeClass;
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
    protected String correlationId;
    protected String containerId;
    protected String modelName;
    protected String fileName;
    protected String targetField;
    protected Object expectedResult;
    protected Map<String, Object> inputData;

//    @BeforeClass
    public static void setup(String resourceDir,
                             long extendedTimeout, String containerId, ReleaseId releaseId) {
        commandsFactory = KieServices.Factory.get().getCommands();
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource(resourceDir);

        // Having timeout issues due to pmml -> raised timeout.
        KieServicesClient client = createDefaultStaticClient(extendedTimeout);
        ServiceResponse<KieContainerResource> reply = client.createContainer(containerId, new KieContainerResource(containerId, releaseId));
        KieServerAssert.assertSuccess(reply);
    }

    public PMMLApplyModelBaseTest(String correlationId, String containerId,
                                  String modelName, String fileName, String targetField, Object expectedResult,
                                  Map<String, Object> inputData) {
        this.correlationId = correlationId;
        this.containerId = containerId;
        this.modelName = modelName;
        this.fileName = fileName;
        this.targetField = targetField;
        this.expectedResult = expectedResult;
        this.inputData = inputData;
    }

    protected void execute() {
        System.out.println(correlationId);
        System.out.println(containerId);
        System.out.println(modelName);
        System.out.println(fileName);
        System.out.println( targetField);
        System.out.println(expectedResult);
        System.out.println(inputData);


        final PMMLRequestData request = new PMMLRequestData(correlationId, modelName);
        request.setSource(fileName);
        inputData.forEach(request::addRequestParam);
        System.out.println(request);

        final ApplyPmmlModelCommand command = (ApplyPmmlModelCommand) commandsFactory.newApplyPmmlModel(request);
        System.out.println(command);
        System.out.println(ruleClient);
        final ServiceResponse<ExecutionResults> results = ruleClient.executeCommandsWithResults(containerId, command);
        System.out.println(results);

        Assertions.assertThat(results.getResult()).isNotNull();
        System.out.println(results.getResult());
        Assertions.assertThat(results.getResult().getValue("results")).isNotNull();
        System.out.println(results.getResult().getValue("results"));

        final PMML4Result resultHolder = (PMML4Result) results.getResult().getValue("results");
        Assertions.assertThat(resultHolder).isNotNull();
        System.out.println(resultHolder);
        System.out.println(resultHolder.getResultCode());
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
