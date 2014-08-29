package org.kie.remote.services.ws.sei.process;

import javax.xml.bind.annotation.XmlType;

/**
 * Only used for initial WSDL generation
 */
@XmlType
public enum WorkItemOperationType {
    ABORT, COMPLETE, GET_INFO;
}
