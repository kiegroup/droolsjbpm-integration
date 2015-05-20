package org.kie.spring.jbpm;

import static org.junit.Assert.*;

import org.junit.Test;
import org.kie.api.runtime.manager.RuntimeManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Test verifying that multiple beans created by RuntimeManagerFactoryBean with same identifier
 * reference to same runtime manager instance.
 */
public class MultipleRuntimeManagersTest extends AbstractJbpmSpringTest {

    @Test
    public void testGettingMultipleRuntimeManagersByContextRetrieve() throws Exception {

        context = new ClassPathXmlApplicationContext("jbpm/multiple-runtime-managers/local-emf-singleton.xml");

        RuntimeManager managerOne = (RuntimeManager) context.getBean("runtimeManager");
        RuntimeManager managerTwo = (RuntimeManager) context.getBean("runtimeManagerTwo");

        assertEquals(managerOne, managerTwo);
    }
}
