package org.kie.server.spring.boot.autoconfiguration.audit.replication;

import org.jbpm.springboot.persistence.JBPMPersistenceUnitPostProcessor;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;


public class OverrideIdJBPMPersistenceUnitPostProcessor implements JBPMPersistenceUnitPostProcessor {

    @Override
    public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
        pui.addManagedPackage("org.kie.server.spring.boot.autoconfiguration.audit.replication");
    }

}
