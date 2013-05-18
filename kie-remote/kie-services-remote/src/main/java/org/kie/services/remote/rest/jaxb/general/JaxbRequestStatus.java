package org.kie.services.remote.rest.jaxb.general;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum JaxbRequestStatus {
    SUCCESS,FAILURE,BAD_REQUEST;
}
