package org.jbpm.task.service.test.spring;

import static org.jbpm.task.service.base.async.TaskServiceLifeCycleBaseAsyncTest.*;
import org.junit.Ignore;

import org.junit.Test;

public class TaskServiceLifeCycleSpringTest extends BaseSpringTest {

     @Test
    public void dummyTest(){
    
    }
    @Ignore
    @Test
    public void testNewTaskWithNoPotentialOwners() {
        runTestNewTaskWithNoPotentialOwners(client, users, groups);
    }
    @Ignore
    @Test
    public void testNewTaskWithSinglePotentialOwner() {
        runTestNewTaskWithSinglePotentialOwner(client, users, groups);
    }
    @Ignore
    @Test
    public void testNewTaskWithContent() {
        runTestNewTaskWithContent(client, users, groups);
    }
    @Ignore
    @Test
    public void testNewTaskWithLargeContent() {
        runTestNewTaskWithLargeContent(client, users, groups);
    }
    @Ignore
    @Test
    public void testClaimWithMultiplePotentialOwners() throws Exception {
        runTestClaimWithMultiplePotentialOwners(client, users, groups);
    }
    @Ignore
    @Test
    public void testClaimWithGroupAssignee() throws Exception {
        runTestClaimWithGroupAssignee(client, users, groups);
    }
    @Ignore
    @Test
    public void testStartFromReadyStateWithPotentialOwner() throws Exception {
        runTestStartFromReadyStateWithPotentialOwner(client, users, groups);
    }
    @Ignore
    @Test
    public void testStartFromReadyStateWithIncorrectPotentialOwner() {
        runTestStartFromReadyStateWithIncorrectPotentialOwner(client, users, groups);
    }
    @Ignore
    @Test
    public void testStartFromReserved() throws Exception {
        runTestStartFromReserved(client, users, groups);
    }
    @Ignore
    @Test
    public void testStartFromReservedWithIncorrectUser() {
        runTestStartFromReservedWithIncorrectUser(client, users, groups);
    }
    @Ignore
    @Test
    public void testStop() {
        runTestStop(client, users, groups);
    }
    @Ignore
    @Test
    public void testStopWithIncorrectUser() {
        runTestStopWithIncorrectUser(client, users, groups);
    }
    @Ignore
    @Test
    public void testReleaseFromInprogress() throws Exception {
        runTestReleaseFromInprogress(client, users, groups);
    }
    @Ignore
    @Test
    public void testReleaseFromReserved() {
        runTestReleaseFromReserved(client, users, groups);
    }
    @Ignore
    @Test
    public void testReleaseWithIncorrectUser() {
        runTestReleaseWithIncorrectUser(client, users, groups);
    }
    @Ignore
    @Test
    public void testSuspendFromReady() {
        runTestSuspendFromReady(client, users, groups);
    }
    @Ignore
    @Test
    public void testSuspendFromReserved() {
        runTestSuspendFromReserved(client, users, groups);
    }
    @Ignore
    @Test
    public void testSuspendFromReservedWithIncorrectUser() {
        runtestSuspendFromReservedWithIncorrectUser(client, users, groups);
    }
    @Ignore
    @Test
    public void testResumeFromReady() {
        runTestResumeFromReady(client, users, groups);
    }
    @Ignore
    @Test
    public void testResumeFromReserved() {
        runTestResumeFromReserved(client, users, groups);
    }
    @Ignore
    @Test
    public void testResumeFromReservedWithIncorrectUser() {
        runTestResumeFromReservedWithIncorrectUser(client, users, groups);
    }
    @Ignore
    @Test
    public void testSkipFromReady() {
        runTestSkipFromReady(client, users, groups);
    }
    @Ignore
    @Test
    public void testSkipFromReserved() {
        runTestSkipFromReserved(client, users, groups);
    }
    @Ignore
    @Test
    public void testDelegateFromReady() throws Exception {
        runTestDelegateFromReady(client, users, groups);
    }
    @Ignore
    @Test
    public void testDelegateFromReserved() throws Exception {
        runTestDelegateFromReserved(client, users, groups);
    }
    @Ignore
    @Test
    public void testDelegateFromReservedWithIncorrectUser() throws Exception {
        runTestDelegateFromReservedWithIncorrectUser(client, users, groups);
    }
    @Ignore
    @Test
    public void testForwardFromReady() throws Exception {
        runTestForwardFromReady(client, users, groups);
    }
    @Ignore
    @Test
    public void testForwardFromReserved() throws Exception {
        runTestForwardFromReserved(client, users, groups);
    }
    @Ignore
    @Test
    public void testForwardFromReservedWithIncorrectUser() throws Exception {
        runTestForwardFromReservedWithIncorrectUser(client, users, groups);
    }
    @Ignore
    @Test
    public void testComplete() {
        runTestComplete(client, users, groups);
    }
    @Ignore
    @Test
    public void testCompleteWithIncorrectUser() {
        runTestCompleteWithIncorrectUser(client, users, groups);
    }
    @Ignore
    @Test
    public void testCompleteWithContent() {
        runTestCompleteWithContent(client, users, groups);
    }
    @Ignore
    @Test
    public void testFail() {
        runTestFail(client, users, groups);
    }
    @Ignore
    @Test
    public void testFailWithIncorrectUser() {
        runTestFailWithIncorrectUser(client, users, groups);
    }
    @Ignore
    @Test
    public void testFailWithContent() {
        runTestFailWithContent(client, users, groups);
    }
    @Ignore
    @Test
    public void testRegisterRemove() throws Exception {
        runTestRegisterRemove(client, users, groups);
    }
    @Ignore
    @Test
    public void testRemoveNotInRecipientList() {
        runTestRemoveNotInRecipientList(client, users, groups);
    }

    /**
     * Nominate an organization entity to process the task. If it is nominated to one person
     * then the new state of the task is Reserved. If it is nominated to several people then 
     * the new state of the task is Ready. This can only be performed when the task is in the 
     * state Created.
     */
    @Ignore    
    @Test
    public void testNominateOnOtherThanCreated() {
        runTestNominateOnOtherThanCreated(client, users, groups);
    }
    @Ignore   
    @Test
    public void testNominateWithIncorrectUser() {
        runTestNominateWithIncorrectUser(client, users, groups);
    }
    @Ignore   
    @Test
    public void testNominateToUser() {
        runTestNominateToUser(client, users, groups);
    }
    @Ignore   
    @Test
    public void testNominateToGroup() {
        runTestNominateToGroup(client, users, groups);
    }
    @Ignore   
    @Test
    public void testActivate() {
        runTestActivate(client, users, groups);
    }
    @Ignore   
    @Test
    public void testActivateWithIncorrectUser() {
        runTestActivateWithIncorrectUser(client, users, groups);
    }
    @Ignore   
    @Test
    public void testActivateFromIncorrectStatus() {
        runTestActivateFromIncorrectStatus(client, users, groups);
    }
    @Ignore   
    @Test
    public void testExitFromReady() {
        runTestExitFromReady(client, users, groups);
    }
    @Ignore   
    @Test
    public void testExitFromReserved() {
        runTestExitFromReserved(client, users, groups);
    }
    @Ignore   
    @Test
    public void testExitFromInProgress() {
        runTestExitFromInProgress(client, users, groups);
    }
    @Ignore   
    @Test
    public void testExitFromSuspended() {
        runTestExitFromSuspended(client, users, groups);
    }
    @Ignore   
    @Test
    public void testExitPermissionDenied() {
        runTestExitPermissionDenied(client, users, groups);
    }
    @Ignore   
    @Test
    public void testExitNotAvailableToUsers() {
        runTestExitNotAvailableToUsers(client, users, groups);
    }

}
