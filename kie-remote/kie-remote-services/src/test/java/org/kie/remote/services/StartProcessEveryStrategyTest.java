/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.remote.services;

import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.UserTaskService;

public interface StartProcessEveryStrategyTest {

    public static final String TEST_PROCESS_DEF_NAME = "org.test.mock.process";
    public static final long TEST_PROCESS_INST_ID = 4;
    
    public void setProcessServiceMock(ProcessService processServiceMock);
    public void setUserTaskServiceMock(UserTaskService userTaskServiceMock);

    public void setupTestMocks();


}
