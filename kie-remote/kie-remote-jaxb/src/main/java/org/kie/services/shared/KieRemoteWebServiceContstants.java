package org.kie.services.shared;

public class KieRemoteWebServiceContstants {

    private KieRemoteWebServiceContstants() {
       // no public constructor: constants class
    }

    /*
     * Namespaces
     */
    public static final String WS_ADDR_NAMESPACE = "http://www.w3.org/2005/08/addressing";

    public static final String WS_SECURITY_UTILITY_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    /*
     * Header ids
     */
    public static final String MESSAGE_ID = "MessageID";

    public static final String RELATES_TO = "RelatesTo";

    public static final String REPLY_TO = "ReplyTo";
}
