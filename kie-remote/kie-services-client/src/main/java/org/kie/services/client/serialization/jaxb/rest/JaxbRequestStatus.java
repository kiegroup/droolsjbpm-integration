package org.kie.services.client.serialization.jaxb.rest;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum JaxbRequestStatus {
    SUCCESS,FAILURE,BAD_REQUEST;
}
