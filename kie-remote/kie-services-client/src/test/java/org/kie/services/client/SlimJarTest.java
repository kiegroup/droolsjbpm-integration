package org.kie.services.client;

import java.lang.reflect.Constructor;

import org.junit.Test;
import org.kie.services.client.api.command.AcceptedCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlimJarTest {

    protected static final Logger logger = LoggerFactory.getLogger(SlimJarTest.class);
    
    @Test
    public void classPathTest() throws Exception { 
        for( Class cmdClass : AcceptedCommands.getSet() ) { 
            Class [] params = {};
            Constructor cmdCtor = cmdClass.getConstructor();
            cmdClass.newInstance();
            logger.debug(cmdClass.getName());
        }
    }
}
