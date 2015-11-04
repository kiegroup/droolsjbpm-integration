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

package org.kie.server.remote.rest.jbpm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.kie.services.impl.form.FormProvider;
import org.jbpm.kie.services.impl.form.provider.ClasspathFormProvider;
import org.jbpm.kie.services.impl.form.provider.InMemoryFormProvider;
import org.jbpm.kie.services.impl.form.provider.InMemoryFormSkeletonProvider;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.kie.api.executor.ExecutorService;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.jbpm.DefinitionServiceBase;
import org.kie.server.services.jbpm.ExecutorServiceBase;
import org.kie.server.services.jbpm.FormServiceBase;
import org.kie.server.services.jbpm.JbpmKieServerExtension;
import org.kie.server.services.jbpm.ProcessServiceBase;
import org.kie.server.services.jbpm.RuntimeDataServiceBase;
import org.kie.server.services.jbpm.UserTaskServiceBase;

public class JbpmRestApplicationComponentsService implements KieServerApplicationComponentsService {

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
        UserTaskService userTaskService = null;
        ExecutorService executorService = null;
        DeploymentService deploymentService = null;
        FormManagerService formManagerService = null;
        KieServerRegistry context = null;

        for( Object object : services ) {
            // in case given service is null (meaning was not configured) continue with next one
            if (object == null) {
                continue;
            }
            if( ProcessService.class.isAssignableFrom(object.getClass()) ) {
               processService = (ProcessService) object;
               continue;
            } else if( RuntimeDataService.class.isAssignableFrom(object.getClass()) ) {
               runtimeDataService = (RuntimeDataService) object;
               continue;
            } else if( DefinitionService.class.isAssignableFrom(object.getClass()) ) {
               definitionService = (DefinitionService) object;
               continue;
            } else if( UserTaskService.class.isAssignableFrom(object.getClass()) ) {
                userTaskService = (UserTaskService) object;
                continue;
            } else if( ExecutorService.class.isAssignableFrom(object.getClass()) ) {
                executorService = (ExecutorService) object;
                continue;
            } else if( DeploymentService.class.isAssignableFrom(object.getClass()) ) {
                deploymentService = (DeploymentService) object;
                continue;
            } else if( FormManagerService.class.isAssignableFrom(object.getClass()) ) {
                formManagerService = (FormManagerService) object;
                continue;
            } else if( KieServerRegistry.class.isAssignableFrom(object.getClass()) ) {
                context = (KieServerRegistry) object;
                continue;
            }
        }

        List<Object> components = new ArrayList<Object>(5);
        DefinitionServiceBase definitionServiceBase = new DefinitionServiceBase(definitionService);
        ProcessServiceBase processServiceBase = new ProcessServiceBase(processService, definitionService, runtimeDataService, context);
        UserTaskServiceBase userTaskServiceBase = new UserTaskServiceBase(userTaskService, context);
        RuntimeDataServiceBase runtimeDataServiceBase = new RuntimeDataServiceBase(runtimeDataService, context);
        ExecutorServiceBase executorServiceBase = new ExecutorServiceBase(executorService, context);
        
        FormServiceBase formServiceBase = new FormServiceBase(definitionService, deploymentService, runtimeDataService, userTaskService, context);
        Set<FormProvider> formProviders = new HashSet<FormProvider>();
        
        InMemoryFormSkeletonProvider iprovider = new InMemoryFormSkeletonProvider();
        iprovider.setFormManagerService(formManagerService);
        formProviders.add(iprovider);
        
        formServiceBase.setProviders(formProviders);

        components.add(new ProcessResource(processServiceBase, definitionServiceBase, runtimeDataServiceBase, context));
        components.add(new RuntimeDataResource(runtimeDataServiceBase));
        components.add(new DefinitionResource(definitionServiceBase));
        components.add(new UserTaskResource(userTaskServiceBase));
        components.add(new ExecutorResource(executorServiceBase));
        components.add(new FormResource(formServiceBase));

        return components;
    }

}
