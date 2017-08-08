
package org.kie.remote.services.ws.deployment.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for deploymentOperationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="deploymentOperationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="DEPLOY"/>
 *     &lt;enumeration value="UNDEPLOY"/>
 *     &lt;enumeration value="GET_INFO"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "deploymentOperationType")
@XmlEnum
public enum DeploymentOperationType {

    DEPLOY,
    UNDEPLOY,
    GET_INFO;

    public String value() {
        return name();
    }

    public static DeploymentOperationType fromValue(String v) {
        return valueOf(v);
    }

}
