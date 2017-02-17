package org.kie.server.client.impl;

import static org.kie.server.api.rest.RestURI.DMN_URI;
import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.api.rest.RestURI.build;

import java.util.HashMap;
import java.util.Map;

import org.kie.dmn.core.api.DMNContext;
import org.kie.dmn.core.api.DMNResult;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.Wrapped;
import org.kie.server.api.model.dmn.DMNContextKS;
import org.kie.server.api.model.dmn.DMNResultKS;
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
        public ServiceResponse<DMNResultKS> evaluateAllDecisions(String containerId, DMNContext dmnContext) {
            Object result = null;
            if( config.isRest() ) {
                Map<String, Object> valuesMap = new HashMap<String, Object>();
                valuesMap.put(CONTAINER_ID, containerId);

                DMNContextKS payload = new DMNContextKS( dmnContext.getAll() ); 
                
                return makeHttpPostRequestAndCreateServiceResponse(
                        build(loadBalancer.getUrl(), DMN_URI, valuesMap), payload, DMNResultKS.class);

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
                return (ServiceResponse<DMNResultKS>) ((Wrapped) result).unwrap();
            }
            return (ServiceResponse<DMNResultKS>) result;
        }
}
