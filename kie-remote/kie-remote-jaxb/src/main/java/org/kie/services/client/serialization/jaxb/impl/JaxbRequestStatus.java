package org.kie.services.client.serialization.jaxb.impl;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum JaxbRequestStatus {
    SUCCESS,
    // Technical failure on the server side
    FAILURE,
    // Syntax exception or command not accepted
    BAD_REQUEST,
    // not an allowed command
    FORBIDDEN,
    // task service permissions
    PERMISSIONS_CONFLICT,
    // instance does not exist
    NOT_FOUND;
}
