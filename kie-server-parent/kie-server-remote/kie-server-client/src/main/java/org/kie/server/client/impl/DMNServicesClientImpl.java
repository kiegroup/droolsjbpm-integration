package org.kie.server.client.impl;

import static org.kie.server.api.rest.RestURI.DMN_URI;
import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.api.rest.RestURI.build;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.kie.dmn.core.api.DMNContext;
import org.kie.dmn.core.api.DMNResult;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.Wrapped;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.type.JaxbMap;
import org.kie.server.client.CaseServicesClient;
import org.kie.server.client.DMNServicesClient;
import org.kie.server.client.KieServicesConfiguration;

public class DMNServicesClientImpl extends AbstractKieServicesClientImpl implements DMNServicesClient {

        public DMNServicesClientImpl(KieServicesConfiguration config) {
            super(config);
        }

        public DMNServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
            super(config, classLoader);
        }

        @Override
        public ServiceResponse<String> evaluateAllDecisions(String containerId, DMNContext dmnContext) {
            Object result = null;
            if( config.isRest() ) {
                Map<String, Object> valuesMap = new HashMap<String, Object>();
                valuesMap.put(CONTAINER_ID, containerId);

                return makeHttpPostRequestAndCreateServiceResponse(
                        build(loadBalancer.getUrl(), DMN_URI, valuesMap), dmnContext.getAll(), String.class);

            } else {
//                CommandScript script = new CommandScript( Collections.singletonList(
//                        (KieServerCommand) new DescriptorCommand("CaseService", "startCase", serialize(caseFile), marshaller.getFormat().getType(), new Object[]{containerId, caseDefinitionId})) );
//                ServiceResponse<String> response = (ServiceResponse<String>)
//                        executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);
//
//                throwExceptionOnFailure(response);
//                if (shouldReturnWithNullResponse(response)) {
//                    return null;
//                }
//                result = deserialize(response.getResult(), Object.class);
            }

            if (result instanceof Wrapped) {
                return (ServiceResponse<String>) ((Wrapped) result).unwrap();
            }
            return (ServiceResponse<String>) result;
        }
}
