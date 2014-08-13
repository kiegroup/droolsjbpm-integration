package org.kie.remote.client.jaxb;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class ConversionUtil {

    private static DatatypeFactory datatypeFactory;
    static { 
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch( DatatypeConfigurationException e ) {
            throw new IllegalStateException("Unable to instantiate " + DatatypeFactory.class.getName() + ": " + e.getMessage(), e);
        }
    }
    
    public static Date convertXmlGregCalToDate(XMLGregorianCalendar xmlCal ) { 
        if( xmlCal == null ) { 
            return null;
        }
        return xmlCal.toGregorianCalendar().getTime();
    }
    
    public static XMLGregorianCalendar convertDateToXmlGregorianCalendar(Date date) { 
        if( date != null ) { 
            GregorianCalendar gregorianCal = new GregorianCalendar();
            gregorianCal.setTime(date);
            return datatypeFactory.newXMLGregorianCalendar(gregorianCal);
        }
        return null;
    }
 
}
