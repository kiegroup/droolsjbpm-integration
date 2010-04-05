package org.drools.server.service;

import javax.jws.WebService;

import org.drools.CheckedDroolsException;
import org.drools.server.KnowledgeService;

@WebService(endpointInterface = "org.drools.server.service.KnowledgeServiceSoap", serviceName = "knowledgeService")
public class KnowledgeServiceSoapImpl implements KnowledgeServiceSoap {

	private KnowledgeService service;
	
	public String execute(String command) throws CheckedDroolsException {
		if (command==null || command.length()==0) {
			throw new CheckedDroolsException("Invalid or null command");
		}
		String response = null;
		try {
			response = getService().executeCommand(command);
		} catch (Exception e) {
			throw new CheckedDroolsException(e.getMessage());
		}
        return response;
	}

	public void setService(KnowledgeService service) {
		this.service = service;
	}

	public KnowledgeService getService() {
		return service;
	}

}