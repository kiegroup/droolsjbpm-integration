package org.kie.server.services.taskassigning.core.model.solver.condition;

import org.kie.server.services.taskassigning.core.model.DefaultLabels;
import org.kie.server.services.taskassigning.core.model.ModelConstants;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.User;

import static org.kie.server.services.taskassigning.core.model.solver.TaskHelper.hasAllLabels;
import static org.kie.server.services.taskassigning.core.model.solver.TaskHelper.isPotentialOwner;

public class TaskAssigningConditions {

    private TaskAssigningConditions() {
    }

    /**
     * @param task a task instance for evaluation.
     * @param user a user instance for evaluation.
     * @return true if the given user is enabled and is a potential owner for the task or is the planning user,
     * false in any other case.
     */
    public static boolean userMeetsPotentialOwnerOrPlanningUserCondition(Task task, User user) {
        return user != null && user.isEnabled() && (ModelConstants.IS_PLANNING_USER.test(user.getEntityId()) || isPotentialOwner(task, user));
    }

    /**
     * @param task a task instance for evaluation.
     * @param user a user instance for evaluation.
     * @return true if the given user is enabled and has all the task defined skills if any or is the planning user,
     * false in any other case.
     */
    public static boolean userMeetsRequiredSkillsOrPlanningUserCondition(Task task, User user) {
        return user != null && user.isEnabled() && (ModelConstants.IS_PLANNING_USER.test(user.getEntityId()) || hasAllLabels(task, user, DefaultLabels.SKILLS.name()));
    }
}
