package org.kie.services.remote.rest.jaxb;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum JaxbRequestStatus {
    SUCCESS,FAILURE,BAD_REQUEST;
}
