package org.drools.server.service;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.drools.CheckedDroolsException;
import org.drools.server.KnowledgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RestWS interface to execute commands
 * @author Lucas Amador
 *
 */
@Path("/rest")
public class KnowledgeServiceRest {
	
	private static Logger logger = LoggerFactory.getLogger(KnowledgeServiceRest.class);
	private KnowledgeService service;
	
	@POST()
	@Path("/execute")
	public Response execute(@FormParam("command") String command) {
		if (command==null || command.length()==0) {
			logger.error("Invalid or null command " + command);			
			return Response.status(Status.BAD_REQUEST).build();
		}
		String response;
		try {
			response = getService().executeCommand(command);
		} catch (CheckedDroolsException e) {
			logger.error(e.getMessage());
			return Response.status(Status.BAD_REQUEST).build();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return Response.status(Status.CONFLICT).build();
		}
		ResponseBuilder builder = Response.ok(response, "application/xml");
        return builder.build();
	}

	public void setService(KnowledgeService service) {
		this.service = service;
	}

	public KnowledgeService getService() {
		return service;
	}
	
}
