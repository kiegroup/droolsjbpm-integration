package org.jbpm.springboot.samples.persistence;

import org.springframework.stereotype.Component;

/**
 * Mock JBPMPersistenceUnitPostProcessor for use in the ExtendedEMFBusinessProcessTest.
 */
@Component
public class JBPMPersistenceUnitPostProcessorMock2 extends AbstractJBPMPersistenceUnitPostProcessorMock {

    public static final String NAME = "JBPMPersistenceUnitPostProcessorMock2";

    public JBPMPersistenceUnitPostProcessorMock2() {
        super(NAME);
    }
}
