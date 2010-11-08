package org.drools.grid.hazelcast;

import java.net.InetSocketAddress;
import java.util.Map;

import org.drools.grid.DistributedServiceLookup;
import org.drools.grid.service.directory.WhitePages;

import com.hazelcast.core.Hazelcast;

public class HazelCastServiceLookup
    implements
    DistributedServiceLookup {
    Map<String, InetSocketAddress[]> serviceAddress;

    public HazelCastServiceLookup() {
        serviceAddress = Hazelcast.getMap( "grid-services" );
    }

    public <T> T get(Class<T> serviceClass) {
        if ( serviceClass == WhitePages.class ) {

        }

        // TODO Auto-generated method stub
        return null;
    }

}
