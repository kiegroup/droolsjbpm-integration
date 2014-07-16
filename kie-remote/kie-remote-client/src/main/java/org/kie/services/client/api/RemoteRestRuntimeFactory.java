package org.kie.services.client.api;

import java.net.URL;

/**
 * Please use the fluent API provided by the {@link RemoteRestRuntimeEngineFactory#newBuilder()}
 * </p>
 * This class will be NOT be present in branches after 6.0.x.
 */
@Deprecated
public class RemoteRestRuntimeFactory extends RemoteRestRuntimeEngineFactory {

    @Deprecated
    public RemoteRestRuntimeFactory(String deploymentId, URL baseUrl, String username, String password) {
        super(deploymentId, baseUrl, username, password);
    }
   
    @Deprecated
    public RemoteRestRuntimeFactory(String deploymentId, URL baseUrl, String username, String password, boolean useFormBasedAuth) {
        super(deploymentId, baseUrl, username, password, useFormBasedAuth);
    }
   
    @Deprecated
    public RemoteRestRuntimeFactory(String deploymentId, URL baseUrl, String username, String password, int timeoutInSeconds) {
        super(deploymentId, baseUrl, username, password, timeoutInSeconds);
    }
   
    @Deprecated
    public RemoteRestRuntimeFactory(String deploymentId, URL baseUrl, String username, String password, int timeoutInSeconds, boolean useFormBasedAuth) {
        super(deploymentId, baseUrl, username, password, timeoutInSeconds, useFormBasedAuth);
    }
   
}
