package org.kie.remote.services.ws.sei.task;

import javax.xml.bind.annotation.XmlType;

@XmlType
public enum TaskOperationType {

    ACTIVATE, CLAIM, CLAIM_NEXT_AVAILABLE,
    COMPLETE, DELEGATE, EXIT, 
    FAIL, FORWARD, 
    NOMINATE,
    RELEASE, RESUME,
    SKIP, STOP, SUSPEND
    ;
}
