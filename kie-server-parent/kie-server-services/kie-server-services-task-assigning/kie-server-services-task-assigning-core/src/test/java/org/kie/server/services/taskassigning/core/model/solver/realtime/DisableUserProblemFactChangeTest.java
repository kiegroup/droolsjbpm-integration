package org.kie.server.services.taskassigning.core.model.solver.realtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.services.taskassigning.core.model.DefaultTaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.User;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.server.services.taskassigning.core.model.TestUtil.mockTask;
import static org.kie.server.services.taskassigning.core.model.TestUtil.mockUser;
import static org.kie.server.services.taskassigning.core.model.solver.realtime.ProblemFactChangeUtilTest.assertTaskWasNotReleased;
import static org.kie.server.services.taskassigning.core.model.solver.realtime.ProblemFactChangeUtilTest.assertTaskWasReleased;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DisableUserProblemFactChangeTest {

    private static final String USER_ID = "USER_ID";
    private static final long TASK_ID1 = 1;
    private static final long TASK_ID2 = 2;
    private static final long TASK_ID3 = 3;
    private static final long TASK_ID4 = 4;

    @Mock
    private ScoreDirector<TaskAssigningSolution<?>> scoreDirector;

    private TaskAssigningSolution<?> workingSolution;

    private DisableUserProblemFactChange change;

    private User user;

    @Before
    public void setUp() {
        user = new User(1, USER_ID);
        workingSolution = new DefaultTaskAssigningSolution(1, new ArrayList<>(), new ArrayList<>());
        doReturn(workingSolution).when(scoreDirector).getWorkingSolution();
        change = new DisableUserProblemFactChange(user);
    }

    @Test
    public void getUser() {
        assertThat(change.getUser()).isEqualTo(user);
    }

    @Test
    public void doChange() {
        List<Task> workingUserTasks = Arrays.asList(mockTask(TASK_ID1, true),
                                                    mockTask(TASK_ID2, true),
                                                    mockTask(TASK_ID3, false),
                                                    mockTask(TASK_ID4, false));

        User workingUser = mockUser(USER_ID, workingUserTasks);
        workingUser.setEnabled(true);

        when(scoreDirector.lookUpWorkingObjectOrReturnNull(user)).thenReturn(workingUser);
        change.doChange(scoreDirector);
        verify(scoreDirector).beforeProblemPropertyChanged(workingUser);
        verify(scoreDirector).afterProblemPropertyChanged(workingUser);
        verify(scoreDirector).triggerVariableListeners();
        assertThat(workingUser.isEnabled()).isFalse();

        List<Task> pinnedTasks = Arrays.asList(workingUserTasks.get(0), workingUserTasks.get(1));
        for (Task pinnedTask : pinnedTasks) {
            assertThat(pinnedTask.isPinned()).isTrue();
            assertTaskWasNotReleased(pinnedTask, scoreDirector);
        }

        List<Task> releasedTasks = Arrays.asList(workingUserTasks.get(2), workingUserTasks.get(3));
        releasedTasks.forEach(releasedTask -> assertTaskWasReleased(releasedTask, scoreDirector));
    }

    @Test
    public void doChangeUserDontExists() {
        when(scoreDirector.lookUpWorkingObjectOrReturnNull(user)).thenReturn(null);
        change.doChange(scoreDirector);
        verify(scoreDirector, never()).beforeProblemPropertyChanged(any());
        verify(scoreDirector, never()).afterProblemPropertyChanged(any());
        verify(scoreDirector, never()).triggerVariableListeners();
    }
}
