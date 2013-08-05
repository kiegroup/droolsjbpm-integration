package org.kie.services.client.serialization.jaxb.impl.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class StringMapXmlAdapater extends XmlAdapter<JaxbStringMap, Map<String, String>> {

    @Override
    public Map<String, String> unmarshal(JaxbStringMap xmlMap) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        for( JaxbStringMapEntry xmlEntry : xmlMap.entries ) { 
            map.put(xmlEntry.key, xmlEntry.value);
        }
        return map;
    }

    @Override
    public JaxbStringMap marshal(Map<String, String> map) throws Exception {
        JaxbStringMap xmlMap = new JaxbStringMap();
        for(Entry<String, String> entry : map.entrySet()) {
           xmlMap.addEntry(entry);
        }
        return xmlMap;
    }


}