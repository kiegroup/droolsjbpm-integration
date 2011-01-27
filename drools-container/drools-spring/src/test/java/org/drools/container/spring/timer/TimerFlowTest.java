package org.drools.container.spring.timer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TimerFlowTest {

    private static final Logger            log    = LoggerFactory.getLogger( TimerFlowTest.class );
    private static final String            TMPDIR = System.getProperty( "java.io.tmpdir" );

    private ClassPathXmlApplicationContext ctx;

    @Before
    public void createSpringContext() {
        try {
            log.info( "creating spring context" );
            PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
            Properties properties = new Properties();
            properties.setProperty( "temp.dir",
                                    TMPDIR );
            configurer.setProperties( properties );
            ctx = new ClassPathXmlApplicationContext();
            ctx.addBeanFactoryPostProcessor( configurer );
            ctx.setConfigLocation( "org/drools/container/spring/timer/conf/spring-conf.xml" );
            ctx.refresh();
        } catch ( Exception e ) {
            log.error( "can't create spring context",
                       e );
            throw new RuntimeException( e );
        }
    }

    @Test @Ignore // test randomly fails on some computer architectures.
    public void doTest() throws Exception {
        // TODO do not use Thread.sleep() in MyDroolsBean, but use Object.wait() and Object.notifyAll() or a Latch instead

        MyDroolsBean myDroolsBean = (MyDroolsBean) ctx.getBean( "myDroolsBean" );

        assertEquals( 0,
                      myDroolsBean.getTimerTriggerCount());

        myDroolsBean.initStartDisposeAndLoadSession();

        int n = myDroolsBean.getTimerTriggerCount();
        assertTrue( n > 0 );

        myDroolsBean.endTheProcess();
        assertTrue( myDroolsBean.getTimerTriggerCount() > n );
    }
}
