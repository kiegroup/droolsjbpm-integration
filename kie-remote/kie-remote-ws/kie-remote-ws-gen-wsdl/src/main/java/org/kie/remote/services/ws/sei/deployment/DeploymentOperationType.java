package org.kie.remote.services.ws.sei.deployment;

import javax.xml.bind.annotation.XmlType;

@XmlType
public enum DeploymentOperationType {
    DEPLOY, UNDEPLOY, GET_INFO
}
