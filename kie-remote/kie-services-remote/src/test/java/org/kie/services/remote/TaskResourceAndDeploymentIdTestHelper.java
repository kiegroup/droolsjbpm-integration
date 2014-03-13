package org.kie.services.remote;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.jbpm.services.task.commands.ClaimTaskCommand;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.ExitTaskCommand;
import org.jbpm.services.task.commands.GetTaskCommand;
import org.jbpm.services.task.commands.ReleaseTaskCommand;
import org.jbpm.services.task.commands.SkipTaskCommand;
import org.jbpm.services.task.commands.StartTaskCommand;
import org.jbpm.services.task.impl.model.TaskDataImpl;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.internal.task.api.model.InternalTaskData;
import org.kie.services.remote.cdi.DeploymentInfoBean;

public class TaskResourceAndDeploymentIdTestHelper {

    public final static String USER = "user";
    public final static long TASK_ID = 1;
    public final static String DEPLOYMENT_ID = "deployment";
    
    public final static boolean FOR_INDEPENDENT_TASKS = true;
    public final static boolean FOR_PROCESS_TASKS = false;

    public static void setupMocks(TaskResourceAndDeploymentIdTest test, boolean independentTask) {
        // DeploymentInfoBean
        DeploymentInfoBean runtimeMgrMgrMock = spy(new DeploymentInfoBean());
        test.setRuntimeMgrMgrMock(runtimeMgrMgrMock);

        RuntimeEngine runtimeEngineMock = mock(RuntimeEngine.class);
        doReturn(runtimeEngineMock).when(runtimeMgrMgrMock).getRuntimeEngine(anyString(), anyLong());
        // - return mock of TaskService
        InternalTaskService runtimeTaskServiceMock = mock(InternalTaskService.class);
        test.setRuntimeTaskServiceMock(runtimeTaskServiceMock);
        // - throw exception if if you try to get a KieSession via RuntimeEngine
        doThrow(new IllegalStateException("ksession")).when(runtimeEngineMock).getKieSession();
        // - let disposeRuntimeEngine() happen.
        doNothing().when(runtimeMgrMgrMock).disposeRuntimeEngine(any(RuntimeEngine.class));

        // TaskService
        InternalTaskService injectedTaskServiceMock = mock(InternalTaskService.class);
        test.setInjectedTaskServiceMock(injectedTaskServiceMock);

        // - set task instance
        InternalTask task = new TaskImpl();
        task.setId(TASK_ID);
        task.setTaskData(new TaskDataImpl());

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

}
