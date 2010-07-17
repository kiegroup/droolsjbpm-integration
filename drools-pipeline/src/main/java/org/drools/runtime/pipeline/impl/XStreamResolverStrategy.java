/**
 * 
 */
package org.drools.runtime.pipeline.impl;

import com.thoughtworks.xstream.XStream;

public interface XStreamResolverStrategy {
    XStream lookup(String name);
}