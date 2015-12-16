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

package org.kie.spring;

import java.util.Map;

import org.kie.internal.runtime.conf.ObjectModel;
import org.kie.internal.runtime.conf.ObjectModelResolver;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringObjectModelResolver implements ObjectModelResolver, ApplicationContextAware {

    private static final String ID = "spring";

    private ApplicationContext applicationContext;

    @Override
    public Object getInstance(ObjectModel model, ClassLoader cl, Map<String, Object> contextParams) {
        if (applicationContext == null) {
            throw new IllegalStateException("No spring application context provided");
        }
        return applicationContext.getBean(model.getIdentifier());
    }

    @Override
    public boolean accept(String resolverId) {
        if (ID.equals(resolverId)) {
            return true;
        }
        return false;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (this.applicationContext == null) {
            this.applicationContext = applicationContext;
        }
    }
}
