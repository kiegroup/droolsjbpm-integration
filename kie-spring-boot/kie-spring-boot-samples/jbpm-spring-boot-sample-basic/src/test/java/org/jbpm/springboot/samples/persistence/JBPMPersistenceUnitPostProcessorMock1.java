package org.jbpm.springboot.samples.persistence;

import org.springframework.stereotype.Component;

/**
 * Mock JBPMPersistenceUnitPostProcessor for use in the ExtendedEMFBusinessProcessTest.
 */
@Component
public class JBPMPersistenceUnitPostProcessorMock1 extends AbstractJBPMPersistenceUnitPostProcessorMock {

    public static final String NAME = "JBPMPersistenceUnitPostProcessorMock1";

    public JBPMPersistenceUnitPostProcessorMock1() {
        super(NAME);
    }
}
