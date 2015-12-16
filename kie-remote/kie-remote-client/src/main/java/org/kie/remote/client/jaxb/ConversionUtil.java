/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.kie.internal.jaxb.StringKeyObjectValueEntry;
import org.kie.internal.jaxb.StringKeyObjectValueMap;
import org.kie.remote.jaxb.gen.JaxbStringObjectPairArray;
import org.kie.remote.jaxb.gen.OrganizationalEntity;
import org.kie.remote.jaxb.gen.Type;
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
    
    public static StringKeyObjectValueMap convertMapToStringKeyObjectValueMap( Map<String, Object> map ) {
        StringKeyObjectValueMap jaxbMap = new StringKeyObjectValueMap();

        if( map == null || map.isEmpty() ) {
            return jaxbMap;
        }
        for( Entry<String, Object> entry : map.entrySet() ) {
            jaxbMap.addEntry(new StringKeyObjectValueEntry(entry));
        }
        return jaxbMap;
    }
    
    public static List<OrganizationalEntity> convertStringListToGenOrgEntList( List<String> orgEntIdList ) { 
        if( orgEntIdList == null ) { 
            return new ArrayList<OrganizationalEntity>(0);
        }
        List<OrganizationalEntity> genOrgEntList = new ArrayList<OrganizationalEntity>(orgEntIdList.size());
        for( String orgEntId : orgEntIdList ) {
           OrganizationalEntity orgEnt = new OrganizationalEntity();
           orgEnt.setId(orgEntId);
           orgEnt.setType(Type.USER);
        }
        return genOrgEntList;
    }
   
    public static byte [] convertSerializableToByteArray(Serializable input) { 
        byte [] result = null;
       
        return result;
    }
}
