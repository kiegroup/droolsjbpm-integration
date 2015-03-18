package org.kie.integration.eap.maven.eap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.assertEquals;

public class EAPContainerTest {

    private EAPContainer tested;
    
    @Before
    public void setUp() throws Exception {
        // Init the annotated mocks.
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testParseContainer() throws Exception {
        tested = new EAPContainer("EAP-6.1.1");
        assertEquals(tested.getContainerId(), EAPContainer.EAPContainerId.EAP);
        assertEquals(tested.getVersion().toString(), "6.1.1");

        tested = new EAPContainer("AS-7.0");
        assertEquals(tested.getContainerId(), EAPContainer.EAPContainerId.AS);
        assertEquals(tested.getVersion().toString(), "7.0");
    }

    @After
    public void tearDown() throws Exception {

    }
    
}
