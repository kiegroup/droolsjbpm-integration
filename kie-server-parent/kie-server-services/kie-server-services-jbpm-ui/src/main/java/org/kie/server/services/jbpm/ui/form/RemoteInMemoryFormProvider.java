/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.jbpm.ui.form;

import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.kie.services.impl.form.provider.InMemoryFormProvider;
import org.kie.server.services.jbpm.ui.FormServiceBase;
import org.kie.server.services.jbpm.ui.api.UIFormProvider;

public class RemoteInMemoryFormProvider extends InMemoryFormProvider implements UIFormProvider {

    @Override
    public void configure( FormManagerService formManagerService ) {
        setFormManagerService( formManagerService );
    }

    @Override
    public String getType() {
        return FormServiceBase.FormType.FREE_MARKER_TYPE.getName();
    }
}
