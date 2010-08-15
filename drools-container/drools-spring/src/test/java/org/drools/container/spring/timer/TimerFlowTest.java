package org.drools.container.spring.timer;

import java.util.Properties;
import static org.junit.Assert.*;
import org.junit.Before;

import org.junit.Test;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

    @Test
    public void doTest() throws Exception{


        MyDroolsBean myDroolsBean = (MyDroolsBean) ctx.getBean( "myDroolsBean");

        assertEquals(0,myDroolsBean.TIMER_TRIGGER_COUNT);

        myDroolsBean.initStartDisposeAndLoadSession();

        int n = myDroolsBean.TIMER_TRIGGER_COUNT;
        assertTrue(n > 0);

        myDroolsBean.endTheProcess();
        assertTrue(myDroolsBean.TIMER_TRIGGER_COUNT > n);
    }
}
