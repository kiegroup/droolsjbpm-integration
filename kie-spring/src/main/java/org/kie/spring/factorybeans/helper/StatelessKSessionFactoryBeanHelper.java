/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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
package org.kie.spring.factorybeans.helper;

import org.kie.api.runtime.StatelessKieSession;
import org.kie.spring.factorybeans.KSessionFactoryBean;

public class StatelessKSessionFactoryBeanHelper extends KSessionFactoryBeanHelper {

    protected StatelessKieSession kieSession;

    public StatelessKSessionFactoryBeanHelper(KSessionFactoryBean factoryBean, StatelessKieSession kieSession) {
        super(factoryBean);
        this.kieSession = kieSession;
    }

    @Override
    public void internalAfterPropertiesSet() throws Exception {
        // do nothing
    }

    @Override
    public Object internalGetObject() {
        return kieSession;
    }

    @Override
    public Object internalNewObject() {
        if (kieBase != null) {
            return kieBase.newStatelessKieSession(factoryBean.getConf());
        }
        return null;
    }
}
