package org.jbpm.task.assigning;

public enum TestDataSet {

    SET_OF_24TASKS_8USERS_SOLUTION("/data/unsolved/24tasks-8users.xml"),
    SET_OF_50TASKS_5USERS_SOLUTION("/data/unsolved/50tasks-5users.xml"),
    SET_OF_100TASKS_5USERS_SOLUTION("/data/unsolved/100tasks-5users.xml"),
    SET_OF_500TASKS_20USERS_SOLUTION("/data/unsolved/500tasks-20users.xml");

    private String resource;

    TestDataSet(String resource) {
        this.resource = resource;
    }

    public String resource() {
        return resource;
    }
}
