package org.kie.server.remote.rest.jbpm.taskqueries;

import static org.kie.server.api.rest.RestURI.TASKS_GET_FILTERED_URI;
import static org.kie.server.remote.rest.common.util.RestUtils.*;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.kie.server.api.model.instance.TaskInstanceList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.taskqueries.TaskQueryServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("server/" + TASKS_GET_FILTERED_URI)
public class TaskQueryResource {
	
	private static final Logger logger = LoggerFactory.getLogger(TaskQueryResource.class);
	
	private final TaskQueryServiceBase taskQueryServiceBase;
	private KieServerRegistry context;
	
	public TaskQueryResource(TaskQueryServiceBase taskQueryServiceBase, KieServerRegistry context) {
		this.taskQueryServiceBase = taskQueryServiceBase;
		this.context = context;
	}
	
	
	//TODO: Implement RESTful operations.
	@POST
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getHumanTasksWithFilters(@Context HttpHeaders headers, 
			@QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize, 
			String payload) {
		
		String type = getContentType(headers);
		// no container id available so only  used to transfer conversation id if given by client.
		Header conversationIdHeader = buildConversationIdHeader("", context, headers);
		
		TaskInstanceList result = taskQueryServiceBase.getHumanTasksWithFilters(page, pageSize, payload, type);
				
		logger.debug("Returning result of task instance search: {}", result);
				
		
		return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);
	}
	
	
	//TODO: We now only support standard QueryFilterSpec configurations ... Do we also need to support builders???

}


/*

public Response runQueryFiltered(@Context HttpHeaders headers,
@PathParam("queryName") String queryName, @QueryParam("mapper") String mapper, @QueryParam("builder") String builder,
@QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize, String payload) {

String type = getContentType(headers);
// no container id available so only used to transfer conversation id if given by client
Header conversationIdHeader = buildConversationIdHeader("", context, headers);
Object result = null;

if (builder != null && !builder.isEmpty()) {
result = queryDataServiceBase.queryFilteredWithBuilder(queryName, mapper, builder, page, pageSize, payload, type);
} else {
result = queryDataServiceBase.queryFiltered(queryName, mapper, page, pageSize, payload, type);
}
logger.debug("Returning result of process instance search: {}", result);

return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);
}
*/