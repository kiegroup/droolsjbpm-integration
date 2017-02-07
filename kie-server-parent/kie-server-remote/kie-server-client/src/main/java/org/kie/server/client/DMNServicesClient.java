package org.kie.server.client;

import org.kie.dmn.core.api.DMNContext;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.dmn.DMNEvaluationResult;

public interface DMNServicesClient {

    ServiceResponse<DMNEvaluationResult> evaluateAllDecisions(String containerId, DMNContext dmnContext);

}
