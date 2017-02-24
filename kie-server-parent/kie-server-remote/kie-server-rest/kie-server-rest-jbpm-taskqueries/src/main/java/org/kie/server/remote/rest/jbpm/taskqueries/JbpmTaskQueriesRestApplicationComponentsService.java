package org.kie.server.remote.rest.jbpm.taskqueries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jbpm.services.api.query.QueryService;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.jbpm.JbpmKieServerExtension;
import org.kie.server.services.jbpm.taskqueries.JbpmTaskQueriesKieServerExtension;
import org.kie.server.services.jbpm.taskqueries.TaskQueryServiceBase;
import org.kie.server.services.jbpm.taskqueries.util.TaskQueriesStrategyFactory;

public class JbpmTaskQueriesRestApplicationComponentsService implements KieServerApplicationComponentsService {

	private static final String OWNER_EXTENSION = JbpmTaskQueriesKieServerExtension.EXTENSION_NAME;
	
	@Override
	public Collection<Object> getAppComponents(String extension, SupportedTransports type, Object... services) {
		// skip calls from other than owning extension
        if ( !OWNER_EXTENSION.equals( extension )) {
            return Collections.emptyList();
        }
        
        KieServerRegistry context = null;

        QueryService queryService = null;
        
        for ( Object object : services ) {
            // in case given service is null (meaning was not configured) continue with next one
            if ( object == null ) {
                continue;
            } else if ( QueryService.class.isAssignableFrom( object.getClass() ) ) {
            	//TODO: We depend on the service provided by the jBPM extension. Our extension does not load this one itself.
                queryService = (QueryService) object;
                continue;
            } else if( KieServerRegistry.class.isAssignableFrom(object.getClass()) ) {
                context = (KieServerRegistry) object;
                continue;
            }
        }
        
        if (queryService == null) {
        	throw new IllegalStateException("No QueryService found. Unable to bootstrap jBPM TaskQuery Extension.");
        }
        
        TaskQueryServiceBase taskQueryServiceBase = new TaskQueryServiceBase(queryService, context, new TaskQueriesStrategyFactory(context).getTaskQueriesStrategy());
        
        List<Object> components = new ArrayList<Object>( 1 );
        components.add( new TaskQueryResource( taskQueryServiceBase, context ) );
        
        return components;
	}

}
