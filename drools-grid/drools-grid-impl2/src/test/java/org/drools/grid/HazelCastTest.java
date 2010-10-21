package org.drools.grid;

import junit.framework.TestCase;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelCastTest extends TestCase {
    public void test1() {
        Config c1 = new XmlConfigBuilder().build();
//        c1.setPortAutoIncrement(false);
//        c1.setPort(5709);

        
        HazelcastInstance hci1 = Hazelcast.newHazelcastInstance( null );
        //hci1.
        System.out.println( hci1.getConfig().getPort() );
        
        Config c2 = new XmlConfigBuilder().build();
//        c2.setPortAutoIncrement(false);
//        c2.setPort(5710);
        HazelcastInstance hci2 = Hazelcast.newHazelcastInstance( null );
        System.out.println( hci2.getConfig().getPort() );
        //System.out.println( hci1.getConfig().get);
    }
}
