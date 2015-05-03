package org.kie.server.remote.rest.jbpm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.jbpm.JbpmKieServerExtension;

public class KieServerJbpmRestApplicationComponentsService implements KieServerApplicationComponentsService {

    private static final String OWNER_EXTENSION = JbpmKieServerExtension.EXTENSION_NAME;

    @Override
    public Collection<Object> getAppComponents( String extension, SupportedTransports type, Object... services ) {
        // skip calls from other than owning extension
        if ( !OWNER_EXTENSION.equals(extension) ) {
            return Collections.emptyList();
        }

        ProcessService  processService = null;
        RuntimeDataService runtimeDataService = null;
        DefinitionService definitionService = null;
       
        for( Object object : services ) { 
            if( ProcessService.class.isAssignableFrom(object.getClass()) ) { 
               processService = (ProcessService) object; 
               continue;
            } else if( RuntimeDataService.class.isAssignableFrom(object.getClass()) ) { 
               runtimeDataService = (RuntimeDataService) object; 
               continue;
            } else if( DefinitionService.class.isAssignableFrom(object.getClass()) ) { 
               definitionService = (DefinitionService) object; 
               continue;
            }
        }
        
        List<Object> components = new ArrayList<Object>(2);
        components.add(new ProcessServiceResource(processService, definitionService));
        components.add(new RuntimeDataServiceResource(runtimeDataService));
        
        return components;
    }

}
