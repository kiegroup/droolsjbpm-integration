package org.kie.server.remote.rest.drools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.drools.DroolsKieServerExtension;
import org.kie.server.services.drools.RuleService;

public class KieServerDroolsRestApplicationComponentsService implements KieServerApplicationComponentsService {

    private static final String OWNER_EXTENSION = DroolsKieServerExtension.EXTENSION_NAME;

    @Override
    public Collection<Object> getAppComponents(String extension, SupportedTransports type, Object... services) {
        // skip calls from other than owning extension
        if ( !OWNER_EXTENSION.equals(extension) ) {
            return Collections.emptyList();
        }

        RuleService ruleService = null;

        for( Object object : services ) {
            if( RuleService.class.isAssignableFrom(object.getClass()) ) {
                ruleService = (RuleService) object;
                continue;
            }
        }

        List<Object> components = new ArrayList<Object>(1);
        components.add(new RuleServiceResource(ruleService));
        return components;
    }
}
