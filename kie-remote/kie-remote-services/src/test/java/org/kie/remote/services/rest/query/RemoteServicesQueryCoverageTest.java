package org.kie.remote.services.rest.query;

import static org.jbpm.query.QueryBuilderCoverageTestUtil.queryBuilderCoverageTest;

import org.jbpm.persistence.correlation.JPACorrelationKeyFactory;
import org.jbpm.query.QueryBuilderCoverageTestUtil.ModuleSpecificInputFiller;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.task.model.Status;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.task.query.TaskQueryBuilder;
import org.kie.remote.services.rest.query.RemoteServicesQueryCommandBuilder.OrderBy;

public class RemoteServicesQueryCoverageTest extends AbstractQueryResourceTest {

    @Before
    public void init() {
        runtimeManager = createRuntimeManager(PROCESS_STRING_VAR_FILE);
        engine = getRuntimeEngine();
        ksession = engine.getKieSession();
        taskService = engine.getTaskService();
   
        this.jpaService = new RemoteServicesQueryJPAService(getEmf());
        
        addObjectProcessInstances = false;
        setupTestData();
    }
    
    @After
    public void cleanup() {
        if( runtimeManager != null ) { 
            runtimeManager.disposeRuntimeEngine(engine);
            runtimeManager.close();
        }
    }
   
    private static ModuleSpecificInputFiller inputFiller = new ModuleSpecificInputFiller() {
        
        private final JPACorrelationKeyFactory correlationKeyFactory = new JPACorrelationKeyFactory();
       
        private int orderByType = 0;
        
        @Override
        public Object fillInput( Class type ) {
            if( type.equals(OrderBy.class) ) { 
                return OrderBy.processInstanceId;
            }  else if( type.isArray() ) {  
                Class elemType = type.getComponentType();
                if( elemType.equals(Status.class) ) {
                   Status [] statusArr = { 
                           Status.Completed,
                           Status.Suspended};
                   return statusArr;
                }
                fail( type.getName() );
            }
            return null;
        }
    };
    
    @Test
    public void remoteServicesQueryCoverageTest() {
       RemoteServicesQueryCommandBuilder queryBuilder = new RemoteServicesQueryCommandBuilder("userId", jpaService);
       Class builderClass = RemoteServicesQueryCommandBuilder.class;
       
       queryBuilderCoverageTest(queryBuilder, builderClass, inputFiller);
    }
   
}
