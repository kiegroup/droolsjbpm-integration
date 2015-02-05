package org.kie.services.client.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.kie.remote.client.api.RemoteWebserviceClientBuilder;
import org.kie.remote.client.api.exception.RemoteApiException;
import org.kie.remote.client.jaxb.JaxbCommandsRequest;
import org.kie.remote.client.jaxb.JaxbCommandsResponse;
import org.kie.remote.client.ws.KieRemoteWsAuthenticator;
import org.kie.remote.services.ws.command.generated.CommandWebService;
import org.kie.remote.services.ws.command.generated.Execute;
import org.kie.remote.services.ws.command.generated.ExecuteResponse;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.shared.ServicesVersion;


/**
 * This is the internal implementation of the {@link RemoteWebserviceClientBuilder} class.
 * </p>
 * It takes care of implementing the methods specified as well as managing the 
 * state of the internal {@link RemoteConfiguration} instance.
 */
class RemoteCommandWebserviceClientBuilderImpl extends RemoteWebserviceClientBuilderImpl<CommandWebService> {

    private final static String commandServiceNamespace = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/command";
    private final static QName commandServiceQName = new QName(commandServiceNamespace, "CommandServiceBasicAuth");
  
    @Override
    public CommandWebService buildBasicAuthClient() {
        checkAndFinalizeConfig();
        
        // wsdl authentication
        KieRemoteWsAuthenticator auth = new KieRemoteWsAuthenticator();
        auth.setUserAndPassword(config.getUserName(), config.getPassword()); 
   
        // wsdl URL
        String wsdlLocationSuffix = "ws/CommandService?wsdl";
        URL wsdlUrl;
        try { 
            wsdlUrl = new URL(config.getServerBaseUrl(), wsdlLocationSuffix);
        } catch( MalformedURLException murle ) { 
            throw new IllegalStateException("Checked URL is not correct: [" + config.getServerBaseUrl().toExternalForm() + wsdlLocationSuffix + "]", murle);
        }
       
        // initial client proxy setup
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(CommandWebService.class);
        factory.setWsdlURL(wsdlUrl.toExternalForm());
        factory.setServiceName(commandServiceQName);

        // JAXB: service classes
        Set<Class<?>> allClasses = new HashSet<Class<?>>();
        allClasses.add(JaxbCommandsRequest.class);
        allClasses.add(JaxbCommandsResponse.class);
        allClasses.add(Execute.class);
        allClasses.add(ExecuteResponse.class);
        
        // JAXB: extra classes 
        Set<Class<?>> extraClasses = config.getExtraJaxbClasses();
        if( extraClasses != null && ! extraClasses.isEmpty() ) { 
           allClasses.addAll(extraClasses);
        } 
       
        // JAXB: setup
        JAXBDataBinding jaxbDataBinding;
        try {
            jaxbDataBinding = new JAXBDataBinding(allClasses.toArray(new Class[allClasses.size()]));
        } catch( JAXBException jaxbe ) {
            throw new RemoteApiException("Unable to initialize JAXB context for webservice client", jaxbe);
        }
        factory.getClientFactoryBean().setDataBinding(jaxbDataBinding);
       
        // setup auth
        // - for webservice calls
        String pwd = config.getPassword();
        String user = config.getUserName();
        factory.setUsername(user);
        factory.setPassword(pwd);
       
        CommandWebService commandService = (CommandWebService) factory.create();
        Client proxyClient = ClientProxy.getClient(commandService);
        HTTPConduit conduit = (HTTPConduit) proxyClient.getConduit(); 

        // setup timeout
        HTTPClientPolicy httpClientPolicy = conduit.getClient();
        httpClientPolicy.setConnectionTimeout(config.getTimeout());
        httpClientPolicy.setReceiveTimeout(config.getTimeout());
        
        // if present, add deployment id for JAXB context
        String depId = config.getDeploymentId();
        if( depId != null && ! depId.trim().isEmpty() ) {
            Map<String, List<String>> headers = new HashMap<String, List<String>>(1);
            String [] depIdHeader = { depId };
            headers.put(JaxbSerializationProvider.EXECUTE_DEPLOYMENT_ID_HEADER, Arrays.asList(depIdHeader));
            proxyClient.getRequestContext().put(org.apache.cxf.message.Message.PROTOCOL_HEADERS, headers);
        }
       
        return commandService;
    }

}