package org.kie.services.client.serialization.jaxb.impl.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.kie.api.definition.KieDefinition.KnowledgeType;

public class KnowledgeTypeXmlAdapter extends XmlAdapter<String, KnowledgeType> {

    @Override
    public KnowledgeType unmarshal(String v) throws Exception {
        if( v != null ) { 
        return Enum.valueOf(KnowledgeType.class, v);
        }
        return null;
    }

    @Override
    public String marshal(KnowledgeType v) throws Exception {
        if( v != null ) { 
            return v.name();
        } 
        return null;
    }

}
