
package org.kie.remote.services.ws.deployment.generated;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.kie.remote.services.ws.common.WebServiceFaultInfo;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.kie.remote.services.ws.deployment.generated package. 
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

    private final static QName _Manage_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/deployment", "manage");
    private final static QName _DeploymentServiceException_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/deployment", "DeploymentServiceException");
    private final static QName _GetProcessDefinitionInfo_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/deployment", "getProcessDefinitionInfo");
    private final static QName _DeploymentUnitList_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/deployment", "deployment-unit-list");
    private final static QName _DeploymentUnit_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/deployment", "deployment-unit");
    private final static QName _GetDeploymentInfo_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/deployment", "getDeploymentInfo");
    private final static QName _GetDeploymentInfoResponse_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/deployment", "getDeploymentInfoResponse");
    private final static QName _ManageResponse_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/deployment", "manageResponse");
    private final static QName _GetProcessDefinitionIds_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/deployment", "getProcessDefinitionIds");
    private final static QName _GetProcessDefinitionInfoResponse_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/deployment", "getProcessDefinitionInfoResponse");
    private final static QName _GetProcessDefinitionIdsResponse_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/deployment", "getProcessDefinitionIdsResponse");
    private final static QName _ProcessDefinitionList_QNAME = new QName("http://services.remote.kie.org/6.5.1.1/deployment", "process-definition-list");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.kie.remote.services.ws.deployment.generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetProcessDefinitionIdsResponse }
     * 
     */
    public GetProcessDefinitionIdsResponse createGetProcessDefinitionIdsResponse() {
        return new GetProcessDefinitionIdsResponse();
    }

    /**
     * Create an instance of {@link GetProcessDefinitionIds }
     * 
     */
    public GetProcessDefinitionIds createGetProcessDefinitionIds() {
        return new GetProcessDefinitionIds();
    }

    /**
     * Create an instance of {@link GetProcessDefinitionInfoResponse }
     * 
     */
    public GetProcessDefinitionInfoResponse createGetProcessDefinitionInfoResponse() {
        return new GetProcessDefinitionInfoResponse();
    }

    /**
     * Create an instance of {@link ManageResponse }
     * 
     */
    public ManageResponse createManageResponse() {
        return new ManageResponse();
    }

    /**
     * Create an instance of {@link GetDeploymentInfo }
     * 
     */
    public GetDeploymentInfo createGetDeploymentInfo() {
        return new GetDeploymentInfo();
    }

    /**
     * Create an instance of {@link GetDeploymentInfoResponse }
     * 
     */
    public GetDeploymentInfoResponse createGetDeploymentInfoResponse() {
        return new GetDeploymentInfoResponse();
    }

    /**
     * Create an instance of {@link GetProcessDefinitionInfo }
     * 
     */
    public GetProcessDefinitionInfo createGetProcessDefinitionInfo() {
        return new GetProcessDefinitionInfo();
    }

    /**
     * Create an instance of {@link Manage }
     * 
     */
    public Manage createManage() {
        return new Manage();
    }

    /**
     * Create an instance of {@link DeploymentIdRequest }
     * 
     */
    public DeploymentIdRequest createDeploymentIdRequest() {
        return new DeploymentIdRequest();
    }

    /**
     * Create an instance of {@link DeploymentInfoResponse }
     * 
     */
    public DeploymentInfoResponse createDeploymentInfoResponse() {
        return new DeploymentInfoResponse();
    }

    /**
     * Create an instance of {@link ProcessIdsResponse }
     * 
     */
    public ProcessIdsResponse createProcessIdsResponse() {
        return new ProcessIdsResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Manage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/deployment", name = "manage")
    public JAXBElement<Manage> createManage(Manage value) {
        return new JAXBElement<Manage>(_Manage_QNAME, Manage.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WebServiceFaultInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/deployment", name = "DeploymentServiceException")
    public JAXBElement<WebServiceFaultInfo> createDeploymentServiceException(WebServiceFaultInfo value) {
        return new JAXBElement<WebServiceFaultInfo>(_DeploymentServiceException_QNAME, WebServiceFaultInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetProcessDefinitionInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/deployment", name = "getProcessDefinitionInfo")
    public JAXBElement<GetProcessDefinitionInfo> createGetProcessDefinitionInfo(GetProcessDefinitionInfo value) {
        return new JAXBElement<GetProcessDefinitionInfo>(_GetProcessDefinitionInfo_QNAME, GetProcessDefinitionInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link JaxbDeploymentUnitList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/deployment", name = "deployment-unit-list")
    public JAXBElement<JaxbDeploymentUnitList> createDeploymentUnitList(JaxbDeploymentUnitList value) {
        return new JAXBElement<JaxbDeploymentUnitList>(_DeploymentUnitList_QNAME, JaxbDeploymentUnitList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link JaxbDeploymentUnit }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/deployment", name = "deployment-unit")
    public JAXBElement<JaxbDeploymentUnit> createDeploymentUnit(JaxbDeploymentUnit value) {
        return new JAXBElement<JaxbDeploymentUnit>(_DeploymentUnit_QNAME, JaxbDeploymentUnit.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDeploymentInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/deployment", name = "getDeploymentInfo")
    public JAXBElement<GetDeploymentInfo> createGetDeploymentInfo(GetDeploymentInfo value) {
        return new JAXBElement<GetDeploymentInfo>(_GetDeploymentInfo_QNAME, GetDeploymentInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDeploymentInfoResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/deployment", name = "getDeploymentInfoResponse")
    public JAXBElement<GetDeploymentInfoResponse> createGetDeploymentInfoResponse(GetDeploymentInfoResponse value) {
        return new JAXBElement<GetDeploymentInfoResponse>(_GetDeploymentInfoResponse_QNAME, GetDeploymentInfoResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ManageResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/deployment", name = "manageResponse")
    public JAXBElement<ManageResponse> createManageResponse(ManageResponse value) {
        return new JAXBElement<ManageResponse>(_ManageResponse_QNAME, ManageResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetProcessDefinitionIds }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/deployment", name = "getProcessDefinitionIds")
    public JAXBElement<GetProcessDefinitionIds> createGetProcessDefinitionIds(GetProcessDefinitionIds value) {
        return new JAXBElement<GetProcessDefinitionIds>(_GetProcessDefinitionIds_QNAME, GetProcessDefinitionIds.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetProcessDefinitionInfoResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/deployment", name = "getProcessDefinitionInfoResponse")
    public JAXBElement<GetProcessDefinitionInfoResponse> createGetProcessDefinitionInfoResponse(GetProcessDefinitionInfoResponse value) {
        return new JAXBElement<GetProcessDefinitionInfoResponse>(_GetProcessDefinitionInfoResponse_QNAME, GetProcessDefinitionInfoResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetProcessDefinitionIdsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/deployment", name = "getProcessDefinitionIdsResponse")
    public JAXBElement<GetProcessDefinitionIdsResponse> createGetProcessDefinitionIdsResponse(GetProcessDefinitionIdsResponse value) {
        return new JAXBElement<GetProcessDefinitionIdsResponse>(_GetProcessDefinitionIdsResponse_QNAME, GetProcessDefinitionIdsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link JaxbProcessDefinitionList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.remote.kie.org/6.5.1.1/deployment", name = "process-definition-list")
    public JAXBElement<JaxbProcessDefinitionList> createProcessDefinitionList(JaxbProcessDefinitionList value) {
        return new JAXBElement<JaxbProcessDefinitionList>(_ProcessDefinitionList_QNAME, JaxbProcessDefinitionList.class, null, value);
    }

}
