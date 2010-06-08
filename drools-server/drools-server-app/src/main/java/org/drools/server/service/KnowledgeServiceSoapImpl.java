package org.drools.server.service;

import javax.jws.WebService;

import org.drools.server.KnowledgeService;

@WebService(endpointInterface = "org.drools.server.service.KnowledgeServiceSoap", serviceName = "knowledgeService")
public class KnowledgeServiceSoapImpl
    implements
    KnowledgeServiceSoap {

    private KnowledgeService service;

    public String execute(String command) throws RuntimeException {
        if ( command == null || command.length() == 0 ) {
            throw new RuntimeException( "Invalid or null command" );
        }
        try {
            return getService().executeCommand( command );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    public void setService(KnowledgeService service) {
        this.service = service;
    }

    public KnowledgeService getService() {
        return service;
    }

}