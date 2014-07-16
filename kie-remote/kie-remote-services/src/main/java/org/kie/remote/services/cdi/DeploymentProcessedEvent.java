package org.kie.remote.services.cdi;


/**
 * This event is fired when 
 * 
 *
 */
public class DeploymentProcessedEvent {

    private String deploymentId;

    public DeploymentProcessedEvent(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }
}