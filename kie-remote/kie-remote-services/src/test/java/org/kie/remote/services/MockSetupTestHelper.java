package org.kie.remote.services;

import static org.kie.remote.services.StartProcessEveryStrategyTest.TEST_PROCESS_INST_ID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.drools.core.command.runtime.process.StartProcessCommand;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.runtime.manager.impl.PerProcessInstanceRuntimeManager;
import org.jbpm.runtime.manager.impl.PerRequestRuntimeManager;
import org.jbpm.runtime.manager.impl.RuntimeEngineImpl;
import org.jbpm.runtime.manager.impl.SingletonRuntimeManager;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.task.commands.ClaimTaskCommand;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.ExitTaskCommand;
import org.jbpm.services.task.commands.GetTaskCommand;
import org.jbpm.services.task.commands.GetTasksOwnedCommand;
import org.jbpm.services.task.commands.ReleaseTaskCommand;
import org.jbpm.services.task.commands.SkipTaskCommand;
import org.jbpm.services.task.commands.StartTaskCommand;
import org.jbpm.services.task.impl.factories.TaskFactory;
import org.jbpm.services.task.impl.model.TaskDataImpl;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.internal.task.api.model.InternalTaskData;
import org.kie.remote.services.cdi.DeploymentInfoBean;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class MockSetupTestHelper {

    public static final String USER = "user";
    public static final String PASSWORD = "password";
    
    public static final long TASK_ID = 1;
    public static final String DEPLOYMENT_ID = "deployment";
    
    public static final boolean FOR_INDEPENDENT_TASKS = true;
    public static final boolean FOR_PROCESS_TASKS = false;

    public static void setupTaskMocks(TaskDeploymentIdTest test, boolean independentTask) {
        DeploymentInfoBean runtimeMgrMgrMock = spy(new DeploymentInfoBean());

        RuntimeEngine runtimeEngineMock = mock(RuntimeEngine.class);
        doReturn(runtimeEngineMock).when(runtimeMgrMgrMock).getRuntimeEngine(anyString(), anyLong());
        // - return mock of TaskService
        InternalTaskService runtimeTaskServiceMock = mock(InternalTaskService.class);
        // - throw exception if if you try to get a KieSession via RuntimeEngine
        doThrow(new IllegalStateException("ksession")).when(runtimeEngineMock).getKieSession();
        // - let disposeRuntimeEngine() happen.
        doNothing().when(runtimeMgrMgrMock).disposeRuntimeEngine(any(RuntimeEngine.class));

        // UserTaskService setup
        UserTaskService userTaskServiceMock = mock(UserTaskService.class);
        test.setUserTaskServiceMock(userTaskServiceMock);
       
        if( test.getTasksTest() ) { 
            List<TaskSummary> taskSumList = new ArrayList<TaskSummary>();
            TaskSummaryImpl taskSum = new TaskSummaryImpl(2l, "test-task", "A Test Task Summary", Status.Created, 0, 
                    USER, USER, new Date(), new Date(), new Date(), 
                    "org.test.process", 28l, -1, "org.test.deployment");
            taskSumList.add(taskSum);
            doReturn(taskSumList).when(userTaskServiceMock).execute(any(String.class), any(GetTasksOwnedCommand.class));
        }

        // TaskService
        InternalTaskService injectedTaskServiceMock = mock(InternalTaskService.class);

        // - set task instance
        InternalTask task = new TaskImpl();
        task.setId(TASK_ID);
        task.setTaskData(new TaskDataImpl());
        doReturn(task).when(userTaskServiceMock).getTask(TASK_ID);

        doThrow(new IllegalStateException("Use the getTaskById() method, which should already have been called!"))
            .when(injectedTaskServiceMock).execute(any(GetTaskCommand.class));
        doReturn(task).when(injectedTaskServiceMock).getTaskById(TASK_ID);

        // task is independent
        if (independentTask) {
            // no deployment id == independent task
            ((InternalTaskData) task.getTaskData()).setDeploymentId(null);
            
            // runtime task engine should not be used
            doThrow(new IllegalStateException("The runtime engine TaskService should not be used here!")).when(runtimeEngineMock).getTaskService();

            // - injected task service should execute commands
            doReturn(null).when(injectedTaskServiceMock).execute(any(ClaimTaskCommand.class));
            doReturn(null).when(injectedTaskServiceMock).execute(any(StartTaskCommand.class));
            doReturn(null).when(injectedTaskServiceMock).execute(any(CompleteTaskCommand.class));
            doReturn(null).when(injectedTaskServiceMock).execute(any(ReleaseTaskCommand.class));
            doReturn(null).when(injectedTaskServiceMock).execute(any(ExitTaskCommand.class));
            doReturn(null).when(injectedTaskServiceMock).execute(any(SkipTaskCommand.class));
           
        } else {
            // deployment id available == process task
            ((InternalTaskData) task.getTaskData()).setDeploymentId(DEPLOYMENT_ID);
            
            // - injected task service should only be used to retrieve task
            doReturn(runtimeTaskServiceMock).when(runtimeEngineMock).getTaskService();
            
            // - runtime engine should execute commands affecting kiesssions
            doReturn(null).when(injectedTaskServiceMock).execute(any(ClaimTaskCommand.class));
            doReturn(null).when(runtimeTaskServiceMock).execute(any(StartTaskCommand.class));
            doReturn(null).when(runtimeTaskServiceMock).execute(any(CompleteTaskCommand.class));
            doReturn(null).when(runtimeTaskServiceMock).execute(any(ReleaseTaskCommand.class));
            doReturn(null).when(runtimeTaskServiceMock).execute(any(ExitTaskCommand.class));
            doReturn(null).when(runtimeTaskServiceMock).execute(any(SkipTaskCommand.class));
        }
        


        test.setupTestMocks();
    }

    public static void setupProcessMocks(StartProcessEveryStrategyTest test, RuntimeStrategy strategy) {
        // dep unit (with runtime mgr): 
        // - deployed classes
        DeployedUnit depUnitMock = mock(DeployedUnit.class);
        doReturn(new ArrayList<Class<?>>()).when(depUnitMock).getDeployedClasses();
        // - deployment unit
        DeploymentUnit realDepUnitMock = mock(DeploymentUnit.class);
        doReturn(realDepUnitMock).when(depUnitMock).getDeploymentUnit();
        // - runtime engine
        RuntimeEngineImpl runtimeEngineMock = mock(RuntimeEngineImpl.class);
        RuntimeManager runtimeMgrMock;
        EmptyContext emptyMock = mock(EmptyContext.class);
        switch(strategy) { 
        case PER_PROCESS_INSTANCE: 
            runtimeMgrMock = mock(PerProcessInstanceRuntimeManager.class);
            // this doesn't really do anything since there is no class/cast checking by mockito
            doReturn(runtimeEngineMock).when(runtimeMgrMock).getRuntimeEngine(any(ProcessInstanceIdContext.class));
            // throw exception is EmptyContext.get()
            mockStatic( EmptyContext.class, ProcessInstanceIdContext.class );
            Mockito.when(EmptyContext.get()).thenThrow(new IllegalStateException("A ProcessInstanceIdContext is expected to be used here!"));
            Mockito.when(ProcessInstanceIdContext.get()).then(new Answer<ProcessInstanceIdContext>() {
                int times = 0;
                public ProcessInstanceIdContext answer(InvocationOnMock invocation) throws Throwable {
                    ++times;
                    if( times > 1 ) { 
                        throw new IllegalStateException("A process instance id is expected to be passed, received and used in the second call.");
                    }
                    return new ProcessInstanceIdContext(null);
                }
            });
            break;
        case PER_REQUEST:
            runtimeMgrMock = mock(PerRequestRuntimeManager.class);
            doReturn(runtimeEngineMock).when(runtimeMgrMock).getRuntimeEngine(any(EmptyContext.class));
            
            mockStatic( EmptyContext.class, ProcessInstanceIdContext.class );
            Mockito.when(ProcessInstanceIdContext.get()).thenThrow(new IllegalStateException("A ProcessInstanceIdContext should NOT have been used here!"));
            Mockito.when(EmptyContext.get()).thenReturn(emptyMock);
            break;
        case SINGLETON:
            runtimeMgrMock = mock(SingletonRuntimeManager.class);
            doReturn(runtimeEngineMock).when(runtimeMgrMock).getRuntimeEngine(any(EmptyContext.class));
            
            mockStatic( EmptyContext.class, ProcessInstanceIdContext.class );
            Mockito.when(ProcessInstanceIdContext.get()).thenThrow(new IllegalStateException("A ProcessInstanceIdContext should NOT have been used here!"));
            Mockito.when(EmptyContext.get()).thenReturn(emptyMock);
            break;
        default: 
            throw new IllegalStateException("Unknown runtime strategy: " + strategy );
        }
        doReturn(runtimeMgrMock).when(depUnitMock).getRuntimeManager();
        doReturn(runtimeMgrMock).when(runtimeEngineMock).getManager();
        
        // add deployment unit
//        runtimeMgrMgr.addOnDeploy(new DeploymentEvent(DEPLOYMENT_ID, depUnitMock));
        
        // ksession setup
        KieSession kieSessionMock = mock(KieSession.class);
        doReturn(kieSessionMock).when(runtimeEngineMock).getKieSession();

        // ProcessService setup
        ProcessService processServiceMock = mock(ProcessService.class);
        test.setProcessServiceMock(processServiceMock);

        // UserTaskService setup
        UserTaskService userTaskServiceMock = mock(UserTaskService.class);
        test.setUserTaskServiceMock(userTaskServiceMock);

        // start process
        RuleFlowProcessInstance procInst = new RuleFlowProcessInstance();
        procInst.setId(TEST_PROCESS_INST_ID);
        
        ProcessInstance procInstMock = spy(procInst);
        doReturn(procInstMock).when(kieSessionMock).execute(any(StartProcessCommand.class));

        doReturn(procInstMock).when(processServiceMock).execute(any(String.class), any(StartProcessCommand.class));
        
        // have test setup mocks
        test.setupTestMocks();
    }
}
