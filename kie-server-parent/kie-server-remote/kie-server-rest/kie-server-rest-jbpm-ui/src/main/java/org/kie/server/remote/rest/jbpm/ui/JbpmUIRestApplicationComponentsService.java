/*
 * Copyright 2015 JBoss Inc
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

package org.kie.server.remote.rest.jbpm.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.jbpm.ui.FormServiceBase;
import org.kie.server.services.jbpm.ui.ImageServiceBase;
import org.kie.server.services.jbpm.ui.JBPMUIKieServerExtension;

public class JbpmUIRestApplicationComponentsService implements KieServerApplicationComponentsService {

    private static final String OWNER_EXTENSION = JBPMUIKieServerExtension.EXTENSION_NAME;
    @Override
    public Collection<Object> getAppComponents(String extension, SupportedTransports type, Object... services) {

        // skip calls from other than owning extension
        if ( !OWNER_EXTENSION.equals(extension) ) {
            return Collections.emptyList();
        }

        FormServiceBase formServiceBase = null;
        ImageServiceBase imageServiceBase = null;

        for( Object object : services ) {
            // in case given service is null (meaning was not configured) continue with next one
            if (object == null) {
                continue;
            }
            if (FormServiceBase.class.isAssignableFrom(object.getClass())) {
                formServiceBase = (FormServiceBase) object;
                continue;
            } else if (ImageServiceBase.class.isAssignableFrom(object.getClass())) {
                imageServiceBase = (ImageServiceBase) object;
                continue;
            }
        }

        List<Object> components = new ArrayList<Object>(2);

        components.add(new FormResource(formServiceBase));
        components.add(new ImageResource(imageServiceBase));

        return components;
    }
}
