/*
 * Copyright 2015 JBoss by Red Hat.
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

import org.kie.api.runtime.manager.RuntimeManager;

/**
 * Class used as holder for runtimeManager instance.
 * Used for injection testing.
 */
public class RuntimeManagerHolder {

    private RuntimeManager runtimeManager;

    public RuntimeManagerHolder(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
    }

    public RuntimeManager getRuntimeManager() {
        return runtimeManager;
    }
}
