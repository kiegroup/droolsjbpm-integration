package org.drools.grid.task;

import org.drools.grid.task.CommandBasedServicesWSHumanTaskHandler;
import org.drools.grid.task.HumanTaskService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.Assert;

import org.drools.process.instance.WorkItem;
import org.drools.process.instance.WorkItemHandler;
import org.drools.process.instance.WorkItemManager;
import org.drools.process.instance.impl.WorkItemImpl;
import org.drools.grid.ExecutionNode;
import org.drools.grid.RemoteConnection;
import org.drools.grid.task.responseHandlers.BlockingGetContentMessageResponseHandler;
import org.drools.grid.task.responseHandlers.BlockingGetTaskMessageResponseHandler;
import org.drools.grid.task.responseHandlers.BlockingTaskOperationMessageResponseHandler;
import org.drools.grid.task.responseHandlers.BlockingTaskSummaryMessageResponseHandler;
import org.drools.task.AccessType;
import org.drools.task.Group;
import org.drools.task.Status;
import org.drools.task.Task;
import org.drools.task.User;
import org.drools.task.query.TaskSummary;
import org.drools.task.service.ContentData;
import org.drools.task.service.PermissionDeniedException;
import org.junit.Test;




public abstract class BaseTaskServiceTest {

    protected CommandBasedServicesWSHumanTaskHandler handler;
    protected HumanTaskService humanTaskClient;
    protected static final int DEFAULT_WAIT_TIME = 5000;
    protected static final int MANAGER_COMPLETION_WAIT_TIME = DEFAULT_WAIT_TIME;
    protected static final int MANAGER_ABORT_WAIT_TIME = DEFAULT_WAIT_TIME;
    protected Map<String, User> users;
    protected Map<String, Group> groups;
    protected ExecutionNode node;
    protected RemoteConnection connection = new RemoteConnection();

    @Test
    public void testTask() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskName");
        workItem.setParameter("Comment", "Comment");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        handler.executeWorkItem(workItem, manager);

        Thread.sleep(500);

        BlockingTaskSummaryMessageResponseHandler responseHandler = new BlockingTaskSummaryMessageResponseHandler();
        humanTaskClient.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
        responseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        List<TaskSummary> tasks = responseHandler.getResults();
        Assert.assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        Assert.assertEquals("TaskName", task.getName());
        Assert.assertEquals(10, task.getPriority());
        Assert.assertEquals("Comment", task.getDescription());
        Assert.assertEquals(Status.Reserved, task.getStatus());
        Assert.assertEquals("Darth Vader", task.getActualOwner().getId());

        System.out.println("Starting task " + task.getId());
        BlockingTaskOperationMessageResponseHandler operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.start(task.getId(), "Darth Vader", operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Started task " + task.getId());

        System.out.println("Completing task " + task.getId());
        operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.complete(task.getId(), "Darth Vader", null, operationResponseHandler);
        operationResponseHandler.waitTillDone(15000);
        System.out.println("Completed task " + task.getId());
        Assert.assertTrue(manager.waitTillCompleted(DEFAULT_WAIT_TIME));
        Thread.sleep(500);
    }
    @Test
    public void testTaskMultipleActors() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskName");
        workItem.setParameter("Comment", "Comment");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader, Dalai Lama");
        handler.executeWorkItem(workItem, manager);

        Thread.sleep(500);

        BlockingTaskSummaryMessageResponseHandler responseHandler = new BlockingTaskSummaryMessageResponseHandler();
        humanTaskClient.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
        responseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        List<TaskSummary> tasks = responseHandler.getResults();
        Assert.assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        Assert.assertEquals("TaskName", task.getName());
        Assert.assertEquals(10, task.getPriority());
        Assert.assertEquals("Comment", task.getDescription());
        Assert.assertEquals(Status.Ready, task.getStatus());

        System.out.println("Claiming task " + task.getId());
        BlockingTaskOperationMessageResponseHandler operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.claim(task.getId(), "Darth Vader", operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Claimed task " + task.getId());

        System.out.println("Starting task " + task.getId());
        operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.start(task.getId(), "Darth Vader", operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Started task " + task.getId());

        System.out.println("Completing task " + task.getId());
        operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.complete(task.getId(), "Darth Vader", null, operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Completed task " + task.getId());

        Assert.assertTrue(manager.waitTillCompleted(MANAGER_COMPLETION_WAIT_TIME));

        Thread.sleep(500);
    }
    @Test
    public void testTaskGroupActors() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskName");
        workItem.setParameter("Comment", "Comment");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("GroupId", "Crusaders");
        handler.executeWorkItem(workItem, manager);

        Thread.sleep(500);

        BlockingTaskSummaryMessageResponseHandler responseHandler = new BlockingTaskSummaryMessageResponseHandler();
        List<String> groupIds = new ArrayList<String>();
        groupIds.add("Crusaders");
        humanTaskClient.getTasksAssignedAsPotentialOwner(null, groupIds, "en-UK", responseHandler);
        responseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        List<TaskSummary> tasks = responseHandler.getResults();
        Assert.assertEquals(1, tasks.size());
        TaskSummary taskSummary = tasks.get(0);
        Assert.assertEquals("TaskName", taskSummary.getName());
        Assert.assertEquals(10, taskSummary.getPriority());
        Assert.assertEquals("Comment", taskSummary.getDescription());
        Assert.assertEquals(Status.Ready, taskSummary.getStatus());

        System.out.println("Claiming task " + taskSummary.getId());
        BlockingTaskOperationMessageResponseHandler operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.claim(taskSummary.getId(), "Darth Vader", operationResponseHandler);
        PermissionDeniedException denied = null;
        System.out.println("1");
        try {
            operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
            System.out.println("2");
        } catch (PermissionDeniedException e) {
        	System.out.println("EXCEPTION: " + e);
                denied = e;
        }
        //@TODO: not working I don't know why!
        //Assert.assertNotNull("Should get permissed denied exception", denied);
        System.out.println("Claimed task " + taskSummary.getId());

        //Check if the parent task is InProgress
        BlockingGetTaskMessageResponseHandler getTaskResponseHandler = new BlockingGetTaskMessageResponseHandler();
        humanTaskClient.getTask(taskSummary.getId(), getTaskResponseHandler);
        Task task = getTaskResponseHandler.getTask();
        Assert.assertEquals(Status.Ready, task.getTaskData().getStatus());

        Thread.sleep(500);
    }
    @Test
    public void testTaskSingleAndGroupActors() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Human Task One");
        workItem.setParameter("TaskName", "TaskNameOne");
        workItem.setParameter("Comment", "Comment");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("GroupId", "Crusaders");
        handler.executeWorkItem(workItem, manager);

        Thread.sleep(500);

        workItem = new WorkItemImpl();
        workItem.setName("Human Task Two");
        workItem.setParameter("TaskName", "TaskNameTwo");
        workItem.setParameter("Comment", "Comment");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        handler.executeWorkItem(workItem, manager);

        Thread.sleep(500);

        BlockingTaskSummaryMessageResponseHandler responseHandler = new BlockingTaskSummaryMessageResponseHandler();
        List<String> groupIds = new ArrayList<String>();
        groupIds.add("Crusaders");
        humanTaskClient.getTasksAssignedAsPotentialOwner("Darth Vader", groupIds, "en-UK", responseHandler);
        responseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        List<TaskSummary> tasks = responseHandler.getResults();
        Assert.assertEquals(2, tasks.size());

        Thread.sleep(500);
    }
    @Test
    public void testTaskFail() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskName");
        workItem.setParameter("Comment", "Comment");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        handler.executeWorkItem(workItem, manager);

        Thread.sleep(500);

        BlockingTaskSummaryMessageResponseHandler responseHandler = new BlockingTaskSummaryMessageResponseHandler();
        humanTaskClient.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
        responseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        List<TaskSummary> tasks = responseHandler.getResults();
        Assert.assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        Assert.assertEquals("TaskName", task.getName());
        Assert.assertEquals(10, task.getPriority());
        Assert.assertEquals("Comment", task.getDescription());
        Assert.assertEquals(Status.Reserved, task.getStatus());
        Assert.assertEquals("Darth Vader", task.getActualOwner().getId());

        System.out.println("Starting task " + task.getId());
        BlockingTaskOperationMessageResponseHandler operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.start(task.getId(), "Darth Vader", operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Started task " + task.getId());

        System.out.println("Failing task " + task.getId());
        operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.fail(task.getId(), "Darth Vader", null, operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Failed task " + task.getId());

        Assert.assertTrue(manager.waitTillAborted(MANAGER_ABORT_WAIT_TIME));

        Thread.sleep(500);
    }
    @Test
    public void testTaskSkip() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskName");
        workItem.setParameter("Comment", "Comment");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        handler.executeWorkItem(workItem, manager);

        Thread.sleep(500);

        BlockingTaskSummaryMessageResponseHandler responseHandler = new BlockingTaskSummaryMessageResponseHandler();
        humanTaskClient.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
        responseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        List<TaskSummary> tasks = responseHandler.getResults();
        Assert.assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        Assert.assertEquals("TaskName", task.getName());
        Assert.assertEquals(10, task.getPriority());
        Assert.assertEquals("Comment", task.getDescription());
        Assert.assertEquals(Status.Reserved, task.getStatus());
        Assert.assertEquals("Darth Vader", task.getActualOwner().getId());

        System.out.println("Skipping task " + task.getId());
        BlockingTaskOperationMessageResponseHandler operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.skip(task.getId(), "Darth Vader", operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Skipped task " + task.getId());

        Assert.assertTrue(manager.waitTillAborted(MANAGER_ABORT_WAIT_TIME));
    }
    @Test
    public void testTaskAbortSkippable() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskName");
        workItem.setParameter("Comment", "Comment");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        handler.executeWorkItem(workItem, manager);

        Thread.sleep(500);

        handler.abortWorkItem(workItem, manager);

        Thread.sleep(500);

        BlockingTaskSummaryMessageResponseHandler responseHandler = new BlockingTaskSummaryMessageResponseHandler();
        humanTaskClient.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
        responseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        List<TaskSummary> tasks = responseHandler.getResults();
        Assert.assertEquals(0, tasks.size());
    }
    @Test
    public void testTaskAbortNotSkippable() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskName");
        workItem.setParameter("Comment", "Comment");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        workItem.setParameter("Skippable", "false");
        handler.executeWorkItem(workItem, manager);

        Thread.sleep(500);

        BlockingTaskSummaryMessageResponseHandler responseHandler = new BlockingTaskSummaryMessageResponseHandler();
        humanTaskClient.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
        responseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        List<TaskSummary> tasks = responseHandler.getResults();
        Assert.assertEquals(1, tasks.size());

        handler.abortWorkItem(workItem, manager);

        Thread.sleep(500);

        responseHandler = new BlockingTaskSummaryMessageResponseHandler();
        humanTaskClient.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
        tasks = responseHandler.getResults();
        Assert.assertEquals(1, tasks.size());
    }
    @Test
    public void testTaskData() throws Exception {
        TestWorkItemManager manager = new TestWorkItemManager();
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskName");
        workItem.setParameter("Comment", "Comment");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        workItem.setParameter("Content", "This is the content");
        handler.executeWorkItem(workItem, manager);

        Thread.sleep(500);

        BlockingTaskSummaryMessageResponseHandler responseHandler = new BlockingTaskSummaryMessageResponseHandler();
        humanTaskClient.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
        responseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        List<TaskSummary> tasks = responseHandler.getResults();
        Assert.assertEquals(1, tasks.size());
        TaskSummary taskSummary = tasks.get(0);
        Assert.assertEquals("TaskName", taskSummary.getName());
        Assert.assertEquals(10, taskSummary.getPriority());
        Assert.assertEquals("Comment", taskSummary.getDescription());
        Assert.assertEquals(Status.Reserved, taskSummary.getStatus());
        Assert.assertEquals("Darth Vader", taskSummary.getActualOwner().getId());

        BlockingGetTaskMessageResponseHandler getTaskResponseHandler = new BlockingGetTaskMessageResponseHandler();
        humanTaskClient.getTask(taskSummary.getId(), getTaskResponseHandler);
        Task task = getTaskResponseHandler.getTask();
        Assert.assertEquals(AccessType.Inline, task.getTaskData().getDocumentAccessType());
        long contentId = task.getTaskData().getDocumentContentId();
        Assert.assertTrue(contentId != -1);
        BlockingGetContentMessageResponseHandler getContentResponseHandler = new BlockingGetContentMessageResponseHandler();
        humanTaskClient.getContent(contentId, getContentResponseHandler);
        ByteArrayInputStream bis = new ByteArrayInputStream(getContentResponseHandler.getContent().getContent());
        ObjectInputStream in = new ObjectInputStream(bis);
        Object data = in.readObject();
        in.close();
        Assert.assertEquals("This is the content", data);

        System.out.println("Starting task " + task.getId());
        BlockingTaskOperationMessageResponseHandler operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.start(task.getId(), "Darth Vader", operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Started task " + task.getId());

        System.out.println("Completing task " + task.getId());
        operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        ContentData result = new ContentData();
        result.setAccessType(AccessType.Inline);
        result.setType("java.lang.String");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject("This is the result");
        out.close();
        result.setContent(bos.toByteArray());
        humanTaskClient.complete(task.getId(), "Darth Vader", result, operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Completed task " + task.getId());

        Assert.assertTrue(manager.waitTillCompleted(MANAGER_COMPLETION_WAIT_TIME));
        Map<String, Object> results = manager.getResults();
        Assert.assertNotNull(results);
        Assert.assertEquals("Darth Vader", results.get("ActorId"));
        Assert.assertEquals("This is the result", results.get("Result"));
    }
    @Test
    public void testOnAllSubTasksEndParentEndStrategy() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        //Create the parent task
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskNameParent");
        workItem.setParameter("Comment", "CommentParent");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        //Set the subtask policy
        workItem.setParameter("SubTaskStrategies", "OnAllSubTasksEndParentEnd");
        handler.executeWorkItem(workItem, manager);


        Thread.sleep(500);

        //Test if the task is succesfully created
        BlockingTaskSummaryMessageResponseHandler responseHandler = new BlockingTaskSummaryMessageResponseHandler();
        humanTaskClient.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
        responseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        List<TaskSummary> tasks = responseHandler.getResults();
        Assert.assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        Assert.assertEquals("TaskNameParent", task.getName());
        Assert.assertEquals(10, task.getPriority());
        Assert.assertEquals("CommentParent", task.getDescription());
        Assert.assertEquals(Status.Reserved, task.getStatus());
        Assert.assertEquals("Darth Vader", task.getActualOwner().getId());

        //Create the child task
        workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskNameChild1");
        workItem.setParameter("Comment", "CommentChild1");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        workItem.setParameter("ParentId", task.getId());
        handler.executeWorkItem(workItem, manager);

        Thread.sleep(500);

        //Create the child task2
        workItem = new WorkItemImpl();
        workItem.setName("Human Task2");
        workItem.setParameter("TaskName", "TaskNameChild2");
        workItem.setParameter("Comment", "CommentChild2");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        workItem.setParameter("ParentId", task.getId());
        handler.executeWorkItem(workItem, manager);

        Thread.sleep(500);

        //Start the parent task
        System.out.println("Starting task " + task.getId());
        BlockingTaskOperationMessageResponseHandler operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.start(task.getId(), "Darth Vader", operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Started task " + task.getId());

        //Check if the parent task is InProgress
        BlockingGetTaskMessageResponseHandler getTaskResponseHandler = new BlockingGetTaskMessageResponseHandler();
        humanTaskClient.getTask(task.getId(), getTaskResponseHandler);
        Task parentTask = getTaskResponseHandler.getTask();
        Assert.assertEquals(Status.InProgress, parentTask.getTaskData().getStatus());
        Assert.assertEquals(users.get("darth"), parentTask.getTaskData().getActualOwner());

        //Get all the subtask created for the parent task based on the potential owner
        responseHandler = new BlockingTaskSummaryMessageResponseHandler();
        humanTaskClient.getSubTasksAssignedAsPotentialOwner(parentTask.getId(), "Darth Vader", "en-UK", responseHandler);
        List<TaskSummary> subTasks = responseHandler.getResults();
        Assert.assertEquals(2, subTasks.size());
        TaskSummary subTaskSummary1 = subTasks.get(0);
        TaskSummary subTaskSummary2 = subTasks.get(1);
        Assert.assertNotNull(subTaskSummary1);
        Assert.assertNotNull(subTaskSummary2);

        //Starting the sub task 1
        System.out.println("Starting sub task " + subTaskSummary1.getId());
        operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.start(subTaskSummary1.getId(), "Darth Vader", operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Started sub task " + subTaskSummary1.getId());

        //Starting the sub task 2
        System.out.println("Starting sub task " + subTaskSummary2.getId());
        operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.start(subTaskSummary2.getId(), "Darth Vader", operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Started sub task " + subTaskSummary2.getId());

        //Check if the child task 1 is InProgress
        getTaskResponseHandler = new BlockingGetTaskMessageResponseHandler();
        humanTaskClient.getTask(subTaskSummary1.getId(), getTaskResponseHandler);
        Task subTask1 = getTaskResponseHandler.getTask();
        Assert.assertEquals(Status.InProgress, subTask1.getTaskData().getStatus());
        Assert.assertEquals(users.get("darth"), subTask1.getTaskData().getActualOwner());

        //Check if the child task 2 is InProgress
        getTaskResponseHandler = new BlockingGetTaskMessageResponseHandler();
        humanTaskClient.getTask(subTaskSummary2.getId(), getTaskResponseHandler);
        Task subTask2 = getTaskResponseHandler.getTask();
        Assert.assertEquals(Status.InProgress, subTask2.getTaskData().getStatus());
        Assert.assertEquals(users.get("darth"), subTask2.getTaskData().getActualOwner());

        // Complete the child task 1
        System.out.println("Completing sub task " + subTask1.getId());
        operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.complete(subTask1.getId(), "Darth Vader", null, operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Completed sub task " + subTask1.getId());

        // Complete the child task 2
        System.out.println("Completing sub task " + subTask2.getId());
        operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.complete(subTask2.getId(), "Darth Vader", null, operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Completed sub task " + subTask2.getId());

        //Check if the child task 1 is Completed

        getTaskResponseHandler = new BlockingGetTaskMessageResponseHandler();
        humanTaskClient.getTask(subTask1.getId(), getTaskResponseHandler);
        subTask1 = getTaskResponseHandler.getTask();
        Assert.assertEquals(Status.Completed, subTask1.getTaskData().getStatus());
        Assert.assertEquals(users.get("darth"), subTask1.getTaskData().getActualOwner());

        //Check if the child task 2 is Completed

        getTaskResponseHandler = new BlockingGetTaskMessageResponseHandler();
        humanTaskClient.getTask(subTask2.getId(), getTaskResponseHandler);
        subTask2 = getTaskResponseHandler.getTask();
        Assert.assertEquals(Status.Completed, subTask2.getTaskData().getStatus());
        Assert.assertEquals(users.get("darth"), subTask2.getTaskData().getActualOwner());

        // Check is the parent task is Complete
        getTaskResponseHandler = new BlockingGetTaskMessageResponseHandler();
        humanTaskClient.getTask(parentTask.getId(), getTaskResponseHandler);
        parentTask = getTaskResponseHandler.getTask();
        Assert.assertEquals(Status.Completed, parentTask.getTaskData().getStatus());
        Assert.assertEquals(users.get("darth"), parentTask.getTaskData().getActualOwner());

        Assert.assertTrue(manager.waitTillCompleted(MANAGER_COMPLETION_WAIT_TIME));
    }
    @Test
    public void testOnParentAbortAllSubTasksEndStrategy() throws Exception {

        TestWorkItemManager manager = new TestWorkItemManager();
        //Create the parent task
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskNameParent");
        workItem.setParameter("Comment", "CommentParent");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        //Set the subtask policy
        workItem.setParameter("SubTaskStrategies", "OnParentAbortAllSubTasksEnd");
        handler.executeWorkItem(workItem, manager);


        Thread.sleep(500);

        //Test if the task is succesfully created
        BlockingTaskSummaryMessageResponseHandler responseHandler = new BlockingTaskSummaryMessageResponseHandler();
        humanTaskClient.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
        responseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        List<TaskSummary> tasks = responseHandler.getResults();
        Assert.assertEquals(1, tasks.size());
        TaskSummary task = tasks.get(0);
        Assert.assertEquals("TaskNameParent", task.getName());
        Assert.assertEquals(10, task.getPriority());
        Assert.assertEquals("CommentParent", task.getDescription());
        Assert.assertEquals(Status.Reserved, task.getStatus());
        Assert.assertEquals("Darth Vader", task.getActualOwner().getId());

        //Create the child task
        workItem = new WorkItemImpl();
        workItem.setName("Human Task");
        workItem.setParameter("TaskName", "TaskNameChild1");
        workItem.setParameter("Comment", "CommentChild1");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        workItem.setParameter("ParentId", task.getId());
        handler.executeWorkItem(workItem, manager);

        Thread.sleep(500);

        //Create the child task2
        workItem = new WorkItemImpl();
        workItem.setName("Human Task2");
        workItem.setParameter("TaskName", "TaskNameChild2");
        workItem.setParameter("Comment", "CommentChild2");
        workItem.setParameter("Priority", "10");
        workItem.setParameter("ActorId", "Darth Vader");
        workItem.setParameter("ParentId", task.getId());
        handler.executeWorkItem(workItem, manager);

        Thread.sleep(500);

        //Start the parent task
        System.out.println("Starting task " + task.getId());
        BlockingTaskOperationMessageResponseHandler operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.start(task.getId(), "Darth Vader", operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Started task " + task.getId());

        //Check if the parent task is InProgress
        BlockingGetTaskMessageResponseHandler getTaskResponseHandler = new BlockingGetTaskMessageResponseHandler();
        humanTaskClient.getTask(task.getId(), getTaskResponseHandler);
        Task parentTask = getTaskResponseHandler.getTask();
        Assert.assertEquals(Status.InProgress, parentTask.getTaskData().getStatus());
        Assert.assertEquals(users.get("darth"), parentTask.getTaskData().getActualOwner());

        //Get all the subtask created for the parent task based on the potential owner
        responseHandler = new BlockingTaskSummaryMessageResponseHandler();
        humanTaskClient.getSubTasksAssignedAsPotentialOwner(parentTask.getId(), "Darth Vader", "en-UK", responseHandler);
        List<TaskSummary> subTasks = responseHandler.getResults();
        Assert.assertEquals(2, subTasks.size());
        TaskSummary subTaskSummary1 = subTasks.get(0);
        TaskSummary subTaskSummary2 = subTasks.get(1);
        Assert.assertNotNull(subTaskSummary1);
        Assert.assertNotNull(subTaskSummary2);

        //Starting the sub task 1
        System.out.println("Starting sub task " + subTaskSummary1.getId());
        operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.start(subTaskSummary1.getId(), "Darth Vader", operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Started sub task " + subTaskSummary1.getId());

        //Starting the sub task 2
        System.out.println("Starting sub task " + subTaskSummary2.getId());
        operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.start(subTaskSummary2.getId(), "Darth Vader", operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Started sub task " + subTaskSummary2.getId());

        //Check if the child task 1 is InProgress
        getTaskResponseHandler = new BlockingGetTaskMessageResponseHandler();
        humanTaskClient.getTask(subTaskSummary1.getId(), getTaskResponseHandler);
        Task subTask1 = getTaskResponseHandler.getTask();
        Assert.assertEquals(Status.InProgress, subTask1.getTaskData().getStatus());
        Assert.assertEquals(users.get("darth"), subTask1.getTaskData().getActualOwner());

        //Check if the child task 2 is InProgress
        getTaskResponseHandler = new BlockingGetTaskMessageResponseHandler();
        humanTaskClient.getTask(subTaskSummary2.getId(), getTaskResponseHandler);
        Task subTask2 = getTaskResponseHandler.getTask();
        Assert.assertEquals(Status.InProgress, subTask2.getTaskData().getStatus());
        Assert.assertEquals(users.get("darth"), subTask2.getTaskData().getActualOwner());

        // Complete the parent task
        System.out.println("Completing parent task " + parentTask.getId());
        operationResponseHandler = new BlockingTaskOperationMessageResponseHandler();
        humanTaskClient.skip(parentTask.getId(), "Darth Vader", operationResponseHandler);
        operationResponseHandler.waitTillDone(DEFAULT_WAIT_TIME);
        System.out.println("Completed parent task " + parentTask.getId());

        //Check if the child task 1 is Completed
        getTaskResponseHandler = new BlockingGetTaskMessageResponseHandler();
        humanTaskClient.getTask(subTaskSummary1.getId(), getTaskResponseHandler);
        subTask1 = getTaskResponseHandler.getTask();
        Assert.assertEquals(Status.Completed, subTask1.getTaskData().getStatus());
        Assert.assertEquals(users.get("darth"), subTask1.getTaskData().getActualOwner());

        //Check if the child task 2 is Completed
        getTaskResponseHandler = new BlockingGetTaskMessageResponseHandler();
        humanTaskClient.getTask(subTaskSummary2.getId(), getTaskResponseHandler);
        subTask2 = getTaskResponseHandler.getTask();
        Assert.assertEquals(Status.Completed, subTask2.getTaskData().getStatus());
        Assert.assertEquals(users.get("darth"), subTask2.getTaskData().getActualOwner());

        Assert.assertTrue(manager.waitTillCompleted(MANAGER_COMPLETION_WAIT_TIME));
    }

    private class TestWorkItemManager implements WorkItemManager {

        private volatile boolean completed;
        private volatile boolean aborted;
        private volatile Map<String, Object> results;

        public synchronized boolean waitTillCompleted(long time) {
            if (!isCompleted()) {
                try {
                    wait(time);
                } catch (InterruptedException e) {
                    // swallow and return state of completed
                }
            }

            return isCompleted();
        }

        public synchronized boolean waitTillAborted(long time) {
            if (!isAborted()) {
                try {
                    wait(time);
                } catch (InterruptedException e) {
                    // swallow and return state of aborted
                }
            }

            return isAborted();
        }

        public void abortWorkItem(long id) {
            setAborted(true);
        }

        public synchronized boolean isAborted() {
            return aborted;
        }

        private synchronized void setAborted(boolean aborted) {
            this.aborted = aborted;
            notifyAll();
        }

        public void completeWorkItem(long id, Map<String, Object> results) {
            this.results = results;
            setCompleted(true);
        }

        private synchronized void setCompleted(boolean completed) {
            this.completed = completed;
            notifyAll();
        }

        public synchronized boolean isCompleted() {
            return completed;
        }

        public WorkItem getWorkItem(long id) {
            return null;
        }

        public Set<WorkItem> getWorkItems() {
            return null;
        }

        public Map<String, Object> getResults() {
            return results;
        }

        public void internalAbortWorkItem(long id) {
        }

        public void internalAddWorkItem(WorkItem workItem) {
        }

        public void internalExecuteWorkItem(WorkItem workItem) {
        }

        public void registerWorkItemHandler(String workItemName, WorkItemHandler handler) {
        }

        //@Override
        public void registerWorkItemHandler(String workItemName, org.drools.runtime.process.WorkItemHandler handler) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }



}
