package org.kie.services.client.api.builder;

import org.kie.remote.client.api.RemoteRestRuntimeEngineBuilder;
import org.kie.remote.client.api.RemoteRestRuntimeEngineFactory;
import org.kie.remote.client.api.RemoteRuntimeEngineBuilder;


/**
 * This class will be deleted after the 6.2.x branch. 
 * </p>
 * Please see the {@link RemoteRestRuntimeEngineBuilder} interface.
 */
@Deprecated 
public interface RemoteRestRuntimeEngineFactoryBuilder  extends RemoteRuntimeEngineBuilder<RemoteRestRuntimeEngineBuilder, RemoteRestRuntimeEngineFactory> {

    /**
     * As of the Drools/jBPM 6.2.0 release, form based authentication is no longer necessary 
     * for tomcat instances. All instances of kie-wb, kie-drools-wb, or jbpm-console
     * now always use normal ("preemptive") http authentication. 
     * </p>
     * <b>This method thus no longer does anything</b> and has been deprecated.
     * @param formBasedAuth Indicates whether or not to use form-based authentication.
     * @return The builder instance
     */
    @Deprecated
    RemoteRestRuntimeEngineBuilder useFormBasedAuth(boolean formBasedAuth);
    
}