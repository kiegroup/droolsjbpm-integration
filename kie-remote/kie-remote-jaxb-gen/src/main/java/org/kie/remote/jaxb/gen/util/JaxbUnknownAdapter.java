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
        if ( o instanceof String ) {
            return stringToBase64String((String) o);
        } else if ( o instanceof List ) {
            List v = ( List ) o;
            return new JaxbListWrapper( v.toArray( new Object[v.size()]) );
        } else {
            return o;
        }
    }

    @Override
    public Object unmarshal(Object o) throws Exception {
        if ( o instanceof String ) {
            return base64StringToString((String) o);
        } else if ( o instanceof JaxbListWrapper ) {
            JaxbListWrapper v = ( JaxbListWrapper ) o;
            return Arrays.asList( v.getElements() );
        } else {
            return o;
        }
    }

    static String stringToBase64String(String in) { 
        System.out.println("Encoding : [" + in + "]");
        if( ! ENCODE_STRINGS ) { 
            return in;
        }
        logger.debug("Encoding string to base64 [{}]", in);
        byte[] bytes = stringToBytes(in);
        String out = DatatypeConverter.printBase64Binary(bytes);
        System.out.println( "["  + in + "] -> [" + out +"]");
        return out;
    }

    static String base64StringToString(String in) { 
        System.out.println("Decoding : [" + in + "]");
        if( ! ENCODE_STRINGS ) { 
            return in;
        }
        logger.debug("Decoding string from base64 [{}]", in);
        byte [] bytes = DatatypeConverter.parseBase64Binary(in);
        String out = bytesToString(bytes);
        System.out.println( "["  + in + "] -> [" + out +"]");
        return out;
    }
    
    // The following methods bypass issues with string encoding
    
    private static byte[] stringToBytes( String str ) {
        char[] chars = str.toCharArray();
        byte[] b = new byte[chars.length << 1];
        for( int ic = 0; ic < chars.length; ic++ ) {
            int ib = ic << 1;
            b[ib] = (byte) ((chars[ic] & 0xFF00) >> 8);
            b[ib + 1] = (byte) (chars[ic] & 0x00FF);
        }
        return b;
    }

    private static String bytesToString( byte[] bytes ) {
        char[] chars = new char[bytes.length >> 1];
        for( int ic = 0; ic < chars.length; ic++ ) {
            int ib = ic << 1;
            char c = (char) (((bytes[ib] & 0x00FF) << 8) + (bytes[ib + 1] & 0x00FF));
            chars[ic] = c;
        }
        return new String(chars);
    }
}
