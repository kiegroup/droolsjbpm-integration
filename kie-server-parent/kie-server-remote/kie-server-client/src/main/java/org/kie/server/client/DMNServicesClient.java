package org.kie.server.client;

import org.kie.dmn.core.api.DMNContext;
import org.kie.dmn.core.api.DMNResult;
import org.kie.server.api.model.ServiceResponse;

public interface DMNServicesClient {

    // TODO review this with Mario..
    // this \/ generification below is to avoid from API client seeing the ? extends
    <R extends DMNResult> ServiceResponse<R> evaluateAllDecisions(String containerId, DMNContext dmnContext);

}
