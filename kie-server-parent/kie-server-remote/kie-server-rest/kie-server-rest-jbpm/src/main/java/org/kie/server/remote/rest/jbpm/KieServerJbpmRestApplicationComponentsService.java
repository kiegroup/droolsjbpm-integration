package org.kie.server.remote.rest.jbpm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.SupportedTransports;

public class KieServerJbpmRestApplicationComponentsService implements KieServerApplicationComponentsService {

    @Override
    public Collection<Object> getAppComponents( SupportedTransports type, Object... services ) {
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
