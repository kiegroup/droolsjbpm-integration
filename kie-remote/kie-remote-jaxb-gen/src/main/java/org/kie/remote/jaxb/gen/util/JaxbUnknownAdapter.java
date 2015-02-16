/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.remote.jaxb.gen.util;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JaxbUnknownAdapter extends XmlAdapter<Object, Object> {

    private static final Logger logger = LoggerFactory.getLogger(JaxbUnknownAdapter.class);
    
    private static final boolean ENCODE_STRINGS = Boolean.parseBoolean(System.getProperty("org.kie.xml.encode", "FALSE"));
   
    
    @Override
    public Object marshal(Object o) throws Exception {
        if ( o instanceof List ) {
            List v = ( List ) o;
            return new JaxbListWrapper( v.toArray( new Object[v.size()]) );
        } else {
            return o;
        }
    }

    @Override
    public Object unmarshal(Object o) throws Exception {
        if ( o instanceof JaxbListWrapper ) {
            JaxbListWrapper v = ( JaxbListWrapper ) o;
            return Arrays.asList( v.getElements() );
        } else {
            return o;
        }
    }

}
