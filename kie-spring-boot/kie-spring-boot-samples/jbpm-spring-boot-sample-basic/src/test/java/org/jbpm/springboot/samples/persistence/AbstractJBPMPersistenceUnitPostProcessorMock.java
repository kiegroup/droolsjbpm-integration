package org.jbpm.springboot.samples.persistence;

import org.jbpm.springboot.persistence.JBPMPersistenceUnitPostProcessor;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

public abstract class AbstractJBPMPersistenceUnitPostProcessorMock implements JBPMPersistenceUnitPostProcessor  {

    private int invocations = 0;

    private MutablePersistenceUnitInfo persistenceUnitInfo;

    private String name;

    public AbstractJBPMPersistenceUnitPostProcessorMock(String name) {
        this.name = name;
    }

    @Override
    public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
        invocations++;
    }

    public String getName() {
        return name;
    }

    public int getInvocations() {
        return invocations;
    }

    public MutablePersistenceUnitInfo getPersistenceUnitInfo() {
        return persistenceUnitInfo;
    }
}
