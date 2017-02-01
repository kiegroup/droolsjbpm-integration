package org.kie.server.client;

import org.kie.dmn.core.api.DMNContext;
import org.kie.server.api.model.ServiceResponse;

public interface DMNServicesClient {

    ServiceResponse<String> evaluateAllDecisions(String containerId, DMNContext dmnContext);

}
