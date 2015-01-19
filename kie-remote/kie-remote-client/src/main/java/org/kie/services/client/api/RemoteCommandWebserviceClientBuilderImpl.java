package org.kie.services.client.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.kie.remote.client.api.RemoteWebserviceClientBuilder;
import org.kie.remote.client.ws.KieRemoteWsAuthenticator;
import org.kie.remote.services.ws.command.generated.CommandServiceBasicAuthClient;
import org.kie.remote.services.ws.command.generated.CommandWebService;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.shared.ServicesVersion;

/**
 * This is the internal implementation of the {@link RemoteWebserviceClientBuilder} class.
 * </p>
 * It takes care of implementing the methods specified as well as managing the 
 * state of the internal {@link RemoteConfiguration} instance.
 */
class RemoteCommandWebserviceClientBuilderImpl extends RemoteWebserviceClientBuilderImpl {

    private final static String commandServiceNamespace = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/command";
    private final static QName commandServiceQName = new QName(commandServiceNamespace, "CommandServiceBasicAuth");
  
    private final static String JAXWS_CONNECT_TIMEOUT = "com.sun.xml.internal.ws.connect.timeout";
    private final static String JAXWS_REQUEST_TIMEOUT =  "com.sun.xml.internal.ws.request.timeout";
    
    @Override
    public CommandServiceBasicAuthClient buildBasicAuthClient() {
        checkAndFinalizeConfig();
        KieRemoteWsAuthenticator auth = new KieRemoteWsAuthenticator();
        auth.setUserAndPassword(config.getUserName(), config.getPassword()); 
    
        String wsdlLocationSuffix = "ws/CommandService?wsdl";
        URL serverBaseUrl;
        try { 
            serverBaseUrl = new URL(config.getServerBaseUrl(), wsdlLocationSuffix);
        } catch( MalformedURLException murle ) { 
            throw new IllegalStateException("Checked URL is not correct: [" + config.getServerBaseUrl().toExternalForm() + wsdlLocationSuffix + "]", murle);
        }
        CommandServiceBasicAuthClient client = new CommandServiceBasicAuthClient(serverBaseUrl, commandServiceQName);
        
        // setup auth
        CommandWebService pws = client.getCommandServiceBasicAuthPort();
        Map<String, Object> reqCtx = ((BindingProvider) pws).getRequestContext();
        
        reqCtx.put(BindingProvider.USERNAME_PROPERTY, config.getUserName());
        reqCtx.put(BindingProvider.PASSWORD_PROPERTY, config.getPassword()); 
       
        // setup timeout
        reqCtx.put(JAXWS_REQUEST_TIMEOUT, config.getTimeout());
        reqCtx.put(JAXWS_CONNECT_TIMEOUT, config.getTimeout());
        
        return client;
    }


}
