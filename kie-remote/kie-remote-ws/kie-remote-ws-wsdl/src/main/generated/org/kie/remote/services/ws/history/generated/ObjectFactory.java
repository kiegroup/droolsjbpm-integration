
package org.kie.remote.services.ws.history.generated;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.kie.remote.services.ws.common.WebServiceFaultInfo;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.kie.remote.services.ws.history.generated package. 
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

    private final static QName _FindNodeInstanceLogs_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/history", "findNodeInstanceLogs");
    private final static QName _FindNodeInstanceLogsResponse_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/history", "findNodeInstanceLogsResponse");
    private final static QName _FindProcessInstanceLogs_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/history", "findProcessInstanceLogs");
    private final static QName _FindVariableInstanceLogs_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/history", "findVariableInstanceLogs");
    private final static QName _HistoryServiceException_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/history", "HistoryServiceException");
    private final static QName _FindProcessInstanceLogsResponse_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/history", "findProcessInstanceLogsResponse");
    private final static QName _FindVariableInstanceLogsResponse_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/history", "findVariableInstanceLogsResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.kie.remote.services.ws.history.generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link FindVariableInstanceLogsResponse }
     * 
     */
    public FindVariableInstanceLogsResponse createFindVariableInstanceLogsResponse() {
        return new FindVariableInstanceLogsResponse();
    }

    /**
     * Create an instance of {@link FindProcessInstanceLogsResponse }
     * 
     */
    public FindProcessInstanceLogsResponse createFindProcessInstanceLogsResponse() {
        return new FindProcessInstanceLogsResponse();
    }

    /**
     * Create an instance of {@link FindProcessInstanceLogs }
     * 
     */
    public FindProcessInstanceLogs createFindProcessInstanceLogs() {
        return new FindProcessInstanceLogs();
    }

    /**
     * Create an instance of {@link FindVariableInstanceLogs }
     * 
     */
    public FindVariableInstanceLogs createFindVariableInstanceLogs() {
        return new FindVariableInstanceLogs();
    }

    /**
     * Create an instance of {@link FindNodeInstanceLogsResponse }
     * 
     */
    public FindNodeInstanceLogsResponse createFindNodeInstanceLogsResponse() {
        return new FindNodeInstanceLogsResponse();
    }

    /**
     * Create an instance of {@link FindNodeInstanceLogs }
     * 
     */
    public FindNodeInstanceLogs createFindNodeInstanceLogs() {
        return new FindNodeInstanceLogs();
    }

    /**
     * Create an instance of {@link VariableInstanceLogResponse }
     * 
     */
    public VariableInstanceLogResponse createVariableInstanceLogResponse() {
        return new VariableInstanceLogResponse();
    }

    /**
     * Create an instance of {@link HistoryInstanceLogRequest }
     * 
     */
    public HistoryInstanceLogRequest createHistoryInstanceLogRequest() {
        return new HistoryInstanceLogRequest();
    }

    /**
     * Create an instance of {@link ProcessInstanceLogResponse }
     * 
     */
    public ProcessInstanceLogResponse createProcessInstanceLogResponse() {
        return new ProcessInstanceLogResponse();
    }

    /**
     * Create an instance of {@link NodeInstanceLogResponse }
     * 
     */
    public NodeInstanceLogResponse createNodeInstanceLogResponse() {
        return new NodeInstanceLogResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindNodeInstanceLogs }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/history", name = "findNodeInstanceLogs")
    public JAXBElement<FindNodeInstanceLogs> createFindNodeInstanceLogs(FindNodeInstanceLogs value) {
        return new JAXBElement<FindNodeInstanceLogs>(_FindNodeInstanceLogs_QNAME, FindNodeInstanceLogs.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindNodeInstanceLogsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/history", name = "findNodeInstanceLogsResponse")
    public JAXBElement<FindNodeInstanceLogsResponse> createFindNodeInstanceLogsResponse(FindNodeInstanceLogsResponse value) {
        return new JAXBElement<FindNodeInstanceLogsResponse>(_FindNodeInstanceLogsResponse_QNAME, FindNodeInstanceLogsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindProcessInstanceLogs }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/history", name = "findProcessInstanceLogs")
    public JAXBElement<FindProcessInstanceLogs> createFindProcessInstanceLogs(FindProcessInstanceLogs value) {
        return new JAXBElement<FindProcessInstanceLogs>(_FindProcessInstanceLogs_QNAME, FindProcessInstanceLogs.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindVariableInstanceLogs }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/history", name = "findVariableInstanceLogs")
    public JAXBElement<FindVariableInstanceLogs> createFindVariableInstanceLogs(FindVariableInstanceLogs value) {
        return new JAXBElement<FindVariableInstanceLogs>(_FindVariableInstanceLogs_QNAME, FindVariableInstanceLogs.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WebServiceFaultInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/history", name = "HistoryServiceException")
    public JAXBElement<WebServiceFaultInfo> createHistoryServiceException(WebServiceFaultInfo value) {
        return new JAXBElement<WebServiceFaultInfo>(_HistoryServiceException_QNAME, WebServiceFaultInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindProcessInstanceLogsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/history", name = "findProcessInstanceLogsResponse")
    public JAXBElement<FindProcessInstanceLogsResponse> createFindProcessInstanceLogsResponse(FindProcessInstanceLogsResponse value) {
        return new JAXBElement<FindProcessInstanceLogsResponse>(_FindProcessInstanceLogsResponse_QNAME, FindProcessInstanceLogsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindVariableInstanceLogsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/history", name = "findVariableInstanceLogsResponse")
    public JAXBElement<FindVariableInstanceLogsResponse> createFindVariableInstanceLogsResponse(FindVariableInstanceLogsResponse value) {
        return new JAXBElement<FindVariableInstanceLogsResponse>(_FindVariableInstanceLogsResponse_QNAME, FindVariableInstanceLogsResponse.class, null, value);
    }

}
