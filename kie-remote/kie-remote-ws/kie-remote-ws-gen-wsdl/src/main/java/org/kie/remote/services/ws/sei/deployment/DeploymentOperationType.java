package org.kie.remote.services.ws.sei.deployment;

import javax.xml.bind.annotation.XmlType;

/**
 * Only used for initial WSDL generation
 */
@XmlType
public enum DeploymentOperationType {
    DEPLOY, UNDEPLOY, GET_INFO,
}
