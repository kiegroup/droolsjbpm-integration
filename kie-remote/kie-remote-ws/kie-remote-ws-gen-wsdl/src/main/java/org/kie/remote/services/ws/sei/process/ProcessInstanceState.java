package org.kie.remote.services.ws.sei.process;

import javax.xml.bind.annotation.XmlType;

/**
 * Only used for initial WSDL generation
 */
@XmlType
public enum ProcessInstanceState {
    PENDING, ACTIVE, COMPLETED, ABORTED, SUSPENDED;
}
