package org.kie.server.services.jbpm;
import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc;
import org.jbpm.services.api.query.QueryMapperRegistry;
import org.jbpm.services.api.query.QueryResultMapper;
import org.jbpm.services.api.query.QueryService;
import org.junit.Test;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieServerRegistryImpl;
import org.kie.server.services.impl.storage.file.KieServerStateFileRepository;
import org.mockito.Mockito;


public class QueryDataServiceBaseTest {

    @Test
    public void testLoadDefaultQueryDefinitions() {
       Date today = new Date();
       KieServerEnvironment.setServerId(UUID.randomUUID().toString());
        QueryService queryService = Mockito.mock(QueryService.class);
        
        KieServerRegistry context = new KieServerRegistryImpl();
        context.registerStateRepository(new KieServerStateFileRepository(new File("target")));
        JbpmKieServerExtension extension = new JbpmKieServerExtension();
        extension.setQueryService(queryService);
       extension.setContext(context);
        extension.registerDefaultQueryDefinitions();
        
        QueryDataServiceBase queryServiceBase = new QueryDataServiceBase(queryService,context);
        
        QueryResultMapper<?> resultMapper = QueryMapperRegistry.get().mapperFor("UserTasksWithPotOwners", null);
        
        List<UserTaskInstanceWithPotOwnerDesc> result = new ArrayList<UserTaskInstanceWithPotOwnerDesc>();
        UserTaskInstanceWithPotOwnerDesc resultPO = new org.jbpm.kie.services.impl.model.UserTaskInstanceWithPotOwnerDesc(
                                                                                         "mcivantos", "mcivantos", today,today,
                                                                                         Long.valueOf(1), "test-task", 0, 
                                                                                         Long.valueOf(1), "processId", "Reserved",
                                                                                         "salaboy", "formName", "correlation-123", 
                                                                                         "subject", "container-1");
        result.add(resultPO);

        Object resultQueryBase = queryServiceBase.transform(result, resultMapper);
        assertNotNull(resultQueryBase); 
    }

}
