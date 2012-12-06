package org.drools.grid.time.impl;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.drools.grid.timer.impl.ScheduledJob;
import org.drools.grid.timer.impl.UuidJobHandle;
import org.drools.time.Job;
import org.drools.time.JobContext;
import org.drools.time.JobHandle;
import org.drools.time.Trigger;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ScheduledJobJpaTest {

    private Server server;
    
    @Before
    public void setUp() {
         DeleteDbFiles.execute("~", "mydb", false);

        System.out.println("Staring DB for white pages ...");
        
        try {
            
            server = Server.createTcpServer(new String[] {"-tcp","-tcpAllowOthers","-tcpDaemon","-trace"}).start(); 
        } catch (SQLException ex) {
            System.out.println("ERROR: "+ex.getMessage());
            
        }
        System.out.println("DB for white pages started! ");

    }

    @After
    public void tearDown() {
        
        server.stop();
        
    }
    @Test
    public void test1() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory( "org.drools.grid" );

        UuidJobHandle handle = new UuidJobHandle();
        ScheduledJob sj1 = new ScheduledJob( handle,
                                             new MockJob(),
                                             new MockJobContext( "xxx" ),
                                             new MockTrigger( new Date( 1000 ) ) );
        ScheduledJob sj2 = new ScheduledJob( handle,
                                             new MockJob(),
                                             new MockJobContext( "xxx" ),
                                             new MockTrigger( new Date( 1000 ) ) );

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist( sj1 );
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();

        sj1 = em.find( ScheduledJob.class,
                       sj1.getId() );

        assertEquals( sj2.getId(),
                      sj1.getId() );
        assertEquals( sj2.getJob().getClass(),
                      sj1.getJob().getClass() );
        assertEquals( "xxx",
                      ((MockJobContext) sj1.getJobContext()).getText() );
        assertEquals( new Date( 1000 ),
                      ((MockTrigger) sj1.getTrigger()).hasNextFireTime() );
        assertEquals( new Date( 1000 ),
                      ((MockTrigger) sj1.getTrigger()).nextFireTime() );

    }

    public static class MockJob
        implements
        Job,
        Serializable {
        public void execute(JobContext ctx) {
        }
    }

    public static class MockJobContext
        implements
        JobContext,
        Serializable {
        private String text;

        public MockJobContext() {

        }

        public MockJobContext(String text) {
            this.text = text;
        }

        public JobHandle getJobHandle() {
            return null;
        }

        public void setJobHandle(JobHandle jobHandle) {

        }

        public String getText() {
            return this.text;
        }

    }

    public static class MockTrigger
        implements
        Trigger,
        Serializable {

        private Date date;

        public MockTrigger() {

        }

        public MockTrigger(Date date) {
            this.date = date;
        }

        public Date hasNextFireTime() {
            return this.date;
        }

        public Date nextFireTime() {
            return this.date;
        }

    }

}
