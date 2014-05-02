package org.kie.services.client.serialization.jaxb.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class XmlCdataAdapter extends XmlAdapter<String, String> {

    private static String CDATA_START = "<![CDATA[";
    private static String CDATA_END = "]]>";
    
    @Override
    public String marshal(String arg0) throws Exception {
        return CDATA_START + arg0 + CDATA_END;
    }

    @Override
    public String unmarshal(String arg0) throws Exception {
        if( arg0.startsWith(CDATA_START) ) { 
            // JSON
            return arg0.substring(9, arg0.length()-3);
        }
        return arg0;
    }

}
