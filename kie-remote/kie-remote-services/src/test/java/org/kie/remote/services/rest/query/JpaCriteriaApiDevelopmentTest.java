package org.kie.remote.services.rest.query;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jbpm.services.task.impl.model.OrganizationalEntityImpl;
import org.jbpm.services.task.impl.model.OrganizationalEntityImpl_;
import org.jbpm.services.task.impl.model.PeopleAssignmentsImpl;
import org.jbpm.services.task.impl.model.PeopleAssignmentsImpl_;
import org.jbpm.services.task.impl.model.TaskDataImpl_;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.impl.model.TaskImpl_;
import org.jbpm.services.task.impl.model.UserImpl_;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.model.InternalPeopleAssignments;

//@Ignore
public class JpaCriteriaApiDevelopmentTest extends AbstractQueryResourceTest {

    @Before
    public void init() {
        runtimeManager = createRuntimeManager(PROCESS_STRING_VAR_FILE);
        engine = getRuntimeEngine();
        ksession = engine.getKieSession();
        taskService = engine.getTaskService();

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

    @Test
    public void developmentTest() throws Exception {

        EntityManager em = getEmf().createEntityManager();
        CriteriaBuilder builder = em.getCriteriaBuilder();

        TypedQuery query;
        // TASK
        {
            CriteriaQuery taskQuery = builder.createQuery(TaskImpl.class);
            taskQuery.from(TaskImpl.class);
            query = em.createQuery(taskQuery);
        }
        List<TaskImpl> tasks = query.getResultList();

        int numTasks = tasks.size();
        logger.debug( "Tasks: " + numTasks );

        String potOwner = null;
        long goodTaskId = -1;
        for( TaskImpl task : tasks ) {
           List<OrganizationalEntity> potOwners = task.getPeopleAssignments().getPotentialOwners();
           if( potOwners.size() > 0 ) {
               goodTaskId = task.getId();
               potOwner = potOwners.get(0).getId();
           }
           List<OrganizationalEntity> stakeHolders
               = ((InternalPeopleAssignments) task.getPeopleAssignments()).getTaskStakeholders();
           List<OrganizationalEntity> busAdmins = task.getPeopleAssignments().getBusinessAdministrators();
           User actOwner = task.getTaskData().getActualOwner();
        }
        safeResearch(em, tasks, goodTaskId, potOwner);
    }

    private void safeResearch(EntityManager em, List<TaskImpl> tasks, long taskId, String potOwner) {
        try {
            doResearch(em, tasks, taskId, potOwner);
        } catch( Throwable t ) {
            t.printStackTrace();
        }
    }

    private void doResearch(EntityManager em, List<TaskImpl> tasks, long taskId, String user) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        TypedQuery query;
        // TASK
        {
            CriteriaQuery taskQuery = builder.createQuery(TaskImpl.class);
            Root<TaskImpl> taskRoot = taskQuery.from(TaskImpl.class);

            Join<TaskImpl, PeopleAssignmentsImpl> taskPeopleJoin = taskRoot.join(TaskImpl_.peopleAssignments);
            Join<PeopleAssignmentsImpl, OrganizationalEntityImpl> peoplePotJoin
                = taskRoot.join(TaskImpl_.peopleAssignments).join(PeopleAssignmentsImpl_.potentialOwners, JoinType.LEFT);
            Predicate potOwnerPred = builder.equal( peoplePotJoin.get(OrganizationalEntityImpl_.id), user );

//            Join<PeopleAssignmentsImpl, OrganizationalEntityImpl> stakeHolderJoin = taskPeopleJoin.join(PeopleAssignmentsImpl_.taskStakeholders, JoinType.LEFT);
//            Predicate stakeHolderPred = builder.equal( stakeHolderJoin.get(OrganizationalEntityImpl_.id), user );

            Join<PeopleAssignmentsImpl, OrganizationalEntityImpl> busAdminJoin
                = taskRoot.join(TaskImpl_.peopleAssignments).join(PeopleAssignmentsImpl_.businessAdministrators, JoinType.LEFT);
            Predicate busAdminPred = builder.equal( busAdminJoin.get(OrganizationalEntityImpl_.id), user );

            Predicate taskIdPred = builder.equal( taskRoot.get(TaskImpl_.id), taskId );

//            Predicate actualOwnPred = builder.equal( taskRoot.get(TaskImpl_.taskData).get(TaskDataImpl_.actualOwner).get(UserImpl_.id), user);
//            Predicate initiatorPred = builder.equal( taskRoot.get(TaskImpl_.taskData).get(TaskDataImpl_.createdBy).get(UserImpl_.id), user);

            Predicate joinPred = builder.or( potOwnerPred, busAdminPred );

            Predicate queryPred = builder.and( taskIdPred, potOwnerPred);
            taskQuery.where(queryPred);
            taskQuery.groupBy(taskRoot.get(TaskImpl_.id));

            query = em.createQuery(taskQuery);
        }


        List<TaskImpl> newTasks = query.getResultList();
        int numTasks = newTasks.size();
        List<Long> taskIds = new ArrayList<Long>();
        for( TaskImpl task : newTasks ) {
           taskIds.add(task.getId());
        }
        newTasks.isEmpty();
    }

}
