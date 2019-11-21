package org.jbpm.task.assigning.model.solver;

import org.jbpm.task.assigning.model.Task;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskDifficultyComparatorTest {

    @Test
    public void testCompare() {
        TaskDifficultyComparator comparator = new TaskDifficultyComparator();

        testEquals(comparator,
                   new Task(1, "name", 5),
                   new Task(1, "name", 5));

        testLessThan(comparator,
                     new Task(1, "name", 5),
                     new Task(1, "name", 1));

        testGreaterThan(comparator,
                        new Task(1, "name", 1),
                        new Task(1, "name", 5));

        testLessThan(comparator,
                     new Task(1, "name", 5),
                     new Task(2, "name", 5));

        testGreaterThan(comparator,
                        new Task(2, "name", 5),
                        new Task(1, "name", 5));
    }

    private void testLessThan(TaskDifficultyComparator comparator, Task task1, Task task2) {
        assertThat(comparator.compare(task1, task2)).isLessThan(0);
    }

    private void testEquals(TaskDifficultyComparator comparator, Task task1, Task task2) {
        assertThat(comparator.compare(task1, task2)).isEqualTo(0);
    }

    private void testGreaterThan(TaskDifficultyComparator comparator, Task task1, Task task2) {
        assertThat(comparator.compare(task1, task2)).isGreaterThan(0);
    }
}
