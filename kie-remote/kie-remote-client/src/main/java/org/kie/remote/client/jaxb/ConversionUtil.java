/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.remote.client.jaxb;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.kie.remote.jaxb.gen.JaxbStringObjectPairArray;
import org.kie.remote.jaxb.gen.util.JaxbStringObjectPair;

public class ConversionUtil {

    private static DatatypeFactory datatypeFactory;
    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch( DatatypeConfigurationException e ) {
            throw new IllegalStateException("Unable to instantiate " + DatatypeFactory.class.getName() + ": " + e.getMessage(), e);
        }
    }

    public static Date convertXmlGregCalToDate( XMLGregorianCalendar xmlCal ) {
        if( xmlCal == null ) {
            return null;
        }
        return xmlCal.toGregorianCalendar().getTime();
    }

    public static XMLGregorianCalendar convertDateToXmlGregorianCalendar( Date date ) {
        if( date != null ) {
            GregorianCalendar gregorianCal = new GregorianCalendar();
            gregorianCal.setTime(date);
            return datatypeFactory.newXMLGregorianCalendar(gregorianCal);
        }
        return null;
    }

    public static JaxbStringObjectPairArray convertMapToJaxbStringObjectPairArray( Map<String, Object> map ) {
        JaxbStringObjectPairArray arrayMap = new JaxbStringObjectPairArray();

        if( map == null || map.isEmpty() ) {
            return arrayMap;
        }
        List<JaxbStringObjectPair> items = arrayMap.getItems();
        for( Entry<String, Object> entry : map.entrySet() ) {
            JaxbStringObjectPair pair = new JaxbStringObjectPair();
            pair.setKey(entry.getKey());
            pair.setValue(entry.getValue());
            items.add(pair);
        }
        return arrayMap;
    }
    
}
