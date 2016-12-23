/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.spring.jbpm.tools;

public class IntegrationSpringBase {
    //process ids
    public static final String SAMPLE_HELLO_PROCESS_ID = "com.sample.bpmn.hello";
    public static final String HUMAN_TASK_PROCESS_ID = "org.jboss.qa.bpms.HumanTask";
    public static final String AGU_SAMPLE_PROCESS_ID = "agu.samples.sample1";
    public static final String SCRIPT_TASK_PROCESS_ID = "org.jboss.qa.bpms.ScriptTask";
    //xml basic paths
    public static final String JTA_EM_SINGLETON_PATH = "jbpm/jta-em/singleton.xml";
    public static final String JTA_EMF_SINGLETON_PATH = "jbpm/jta-emf/singleton.xml";
    public static final String LOCAL_EM_SINGLETON_PATH = "jbpm/local-em/singleton.xml";
    public static final String LOCAL_EMF_SINGLETON_PATH = "jbpm/local-emf/singleton.xml";
    public static final String JTA_EM_PER_PROCESS_INSTANCE_PATH = "jbpm/jta-em/per-process-instance.xml";
    public static final String JTA_EMF_PER_PROCESS_INSTANCE_PATH = "jbpm/jta-emf/per-process-instance.xml";
    public static final String LOCAL_EM_PER_PROCESS_INSTANCE_PATH = "jbpm/local-em/per-process-instance.xml";
    public static final String LOCAL_EMF_PER_PROCESS_INSTANCE_PATH = "jbpm/local-emf/per-process-instance.xml";
    public static final String JTA_EM_PER_REQUEST_PATH = "jbpm/jta-em/per-request.xml";
    public static final String JTA_EMF_PER_REQUEST_PATH = "jbpm/jta-emf/per-request.xml";
    public static final String LOCAL_EM_PER_REQUEST_PATH = "jbpm/local-em/per-request.xml";
    public static final String LOCAL_EMF_PER_REQUEST_PATH = "jbpm/local-emf/per-request.xml";
    //xml path for SwimlaneSpringTest
    public static final String USERGROUP_CALLBACK_LOCAL_EMF_SINGLETON_PATH = "jbpm/usergroup-callback/local-emf-singleton.xml";
    //xml paths for UserManagedSharedTaskServiceSpringTest 
    public static final String SHARED_TASKSERVICE_JTA_EM_SINGLETON_PATH = "jbpm/shared-taskservice/jta-em-singleton.xml";
    public static final String SHARED_TASKSERVICE_JTA_EMF_SINGLETON_PATH = "jbpm/shared-taskservice/jta-emf-singleton.xml";
    public static final String SHARED_TASKSERVICE_PER_PROCESS_INSTANCE_PATH = "jbpm/shared-taskservice/jta-emf-per-process-instance.xml";
    public static final String SHARED_TASKSERVICE_PER_REQUEST_PATH = "jbpm/shared-taskservice/jta-emf-per-request.xml";
    //xml path for MultipleRuntimeManagersTest
    public static final String MULTIPLE_RUNTIME_MANAGERS_LOCAL_EMF_SINGLETON_PATH = "jbpm/multiple-runtime-managers/local-emf-singleton.xml";
    //xml paths for PessimisticLockingSpringTest
    public static final String PESSIMISTIC_LOCK_LOCAL_EM_PATH = "jbpm/pessimistic-lock/pessimistic-locking-local-em-factory-beans.xml";
    public static final String PESSIMISTIC_LOCK_LOCAL_EMF_PATH = "jbpm/pessimistic-lock/pessimistic-locking-local-emf-factory-beans.xml";
    //xml paths for RuntimeManagerInitNoInitialContextSpringTest
    public static final String NO_INITIAL_CONTEXT_LOCAL_EMF_SINGLETON_PATH = "jbpm/no-initial-context/local-emf-singleton.xml";
    public static final String NO_INITIAL_CONTEXT_LOCAL_EMF_PER_PROCESS_PATH = "jbpm/no-initial-context/local-emf-per-process.xml";
    public static final String NO_INITIAL_CONTEXT_LOCAL_EMF_PER_REQUEST_PATH = "jbpm/no-initial-context/local-emf-per-request.xml";

    public static final String USER_JOHN = "john";
    public static final String USER_MARY = "mary";
    public static final String USER_MAX = "max";
}
