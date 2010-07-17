/**
 * 
 */
package org.drools.runtime.pipeline.impl;

import java.util.Map;

import com.thoughtworks.xstream.XStream;

public class NamedXstreamLookupStrategy
    implements
    XStreamResolverStrategy {
    private Map<String, XStream> xstreams;
    
    public NamedXstreamLookupStrategy(Map<String, XStream> xstreams) {
        this.xstreams = xstreams;
    }

    public XStream lookup(String name) {
        return this.xstreams.get( name );
    }
    
}