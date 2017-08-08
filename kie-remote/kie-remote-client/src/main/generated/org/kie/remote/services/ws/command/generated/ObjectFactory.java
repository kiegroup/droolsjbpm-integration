
package org.kie.remote.services.ws.command.generated;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.kie.remote.client.jaxb.JaxbCommandsRequest;
import org.kie.remote.client.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.ws.common.WebServiceFaultInfo;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.kie.remote.services.ws.command.generated package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _CommandRequest_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/command", "command-request");
    private final static QName _ExecuteResponse_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/command", "executeResponse");
    private final static QName _Execute_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/command", "execute");
    private final static QName _CommandServiceException_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/command", "CommandServiceException");
    private final static QName _CommandResponse_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/command", "command-response");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.kie.remote.services.ws.command.generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ExecuteResponse }
     * 
     */
    public ExecuteResponse createExecuteResponse() {
        return new ExecuteResponse();
    }

    /**
     * Create an instance of {@link Execute }
     * 
     */
    public Execute createExecute() {
        return new Execute();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link JaxbCommandsRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/command", name = "command-request")
    public JAXBElement<JaxbCommandsRequest> createCommandRequest(JaxbCommandsRequest value) {
        return new JAXBElement<JaxbCommandsRequest>(_CommandRequest_QNAME, JaxbCommandsRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExecuteResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/command", name = "executeResponse")
    public JAXBElement<ExecuteResponse> createExecuteResponse(ExecuteResponse value) {
        return new JAXBElement<ExecuteResponse>(_ExecuteResponse_QNAME, ExecuteResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Execute }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/command", name = "execute")
    public JAXBElement<Execute> createExecute(Execute value) {
        return new JAXBElement<Execute>(_Execute_QNAME, Execute.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WebServiceFaultInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/command", name = "CommandServiceException")
    public JAXBElement<WebServiceFaultInfo> createCommandServiceException(WebServiceFaultInfo value) {
        return new JAXBElement<WebServiceFaultInfo>(_CommandServiceException_QNAME, WebServiceFaultInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link JaxbCommandsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/command", name = "command-response")
    public JAXBElement<JaxbCommandsResponse> createCommandResponse(JaxbCommandsResponse value) {
        return new JAXBElement<JaxbCommandsResponse>(_CommandResponse_QNAME, JaxbCommandsResponse.class, null, value);
    }

}
