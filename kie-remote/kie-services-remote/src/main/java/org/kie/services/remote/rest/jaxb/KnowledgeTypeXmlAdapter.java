package org.kie.services.remote.rest.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.kie.api.definition.KieDefinition.KnowledgeType;

public class KnowledgeTypeXmlAdapter extends XmlAdapter<String, KnowledgeType> {

    @Override
    public KnowledgeType unmarshal(String arg0) throws Exception {
        return Enum.valueOf(KnowledgeType.class, arg0);
    }

    @Override
    public String marshal(KnowledgeType v) throws Exception {
        return v.name();
    }

}
