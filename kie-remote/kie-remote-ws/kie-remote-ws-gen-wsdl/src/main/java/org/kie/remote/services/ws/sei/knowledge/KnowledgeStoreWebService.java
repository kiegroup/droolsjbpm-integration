/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/


package org.kie.remote.services.ws.sei.knowledge;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.kie.remote.services.ws.common.KieRemoteWebServiceException;
import org.kie.remote.services.ws.sei.ServicesVersion;


@WebService(name = "KnowledgeStoreService", targetNamespace = KnowledgeStoreWebService.NAMESPACE)
public interface KnowledgeStoreWebService {

    static final String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/knowledge";
    
    @WebMethod(action = "urn:GetRepositories")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getRepositories", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperRepositoriesRequest")
    @ResponseWrapper(localName = "getRepositories", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperRepositoriesResponse")
    public RepositoryResponse getRepositories(@WebParam(name = "arg0", targetNamespace = "") RepositoriesRequest arg0) throws KieRemoteWebServiceException;

    @WebMethod(action = "urn:ManageRepositories")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "manageRepositories", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperManageRepositoriesRequest")
    @ResponseWrapper(localName = "manageRepositories", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperManageRepositoriesResponse")
    public void manageRepositories(@WebParam(name = "arg0", targetNamespace = "") RepositoryOperationRequest arg0) throws KieRemoteWebServiceException;

    @WebMethod(action = "urn:ManageProjects")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "manageProjects", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperManageProjectsRequest")
    @ResponseWrapper(localName = "manageProjects", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperManageProjectsResponse")
    public ProjectsResponse getProjects(@WebParam(name = "arg0", targetNamespace = "") ProjectsResponse arg0) throws KieRemoteWebServiceException;

    @WebMethod(action = "urn:ManageProjects")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "manageProjects", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperManageProjectsRequest")
    @ResponseWrapper(localName = "manageProjects", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperManageProjectsResponse")
    public void manageProjects(@WebParam(name = "arg0", targetNamespace = "") ProjectOperationRequest arg0) throws KieRemoteWebServiceException;

}
