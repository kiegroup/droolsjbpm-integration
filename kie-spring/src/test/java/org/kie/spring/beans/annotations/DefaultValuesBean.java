/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.spring.beans.annotations;

import org.kie.api.KieBase;
import org.kie.api.cdi.KBase;

public class DefaultValuesBean {

    @KBase
    KieBase kieBase;

    KieBase kieBase2;

    public KieBase getKieBase() {
        return kieBase;
    }
    public void setKieBase(KieBase kieBase) {
        this.kieBase = kieBase;
    }

    public KieBase getKieBase2() {
        return kieBase2;
    }

    @KBase
    public void setKieBase2(KieBase kieBase2) {
        this.kieBase2 = kieBase2;
    }
}
