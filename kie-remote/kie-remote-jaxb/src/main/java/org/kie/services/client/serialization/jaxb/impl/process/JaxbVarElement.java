package org.kie.services.client.serialization.jaxb.impl.process;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class JaxbVarElement extends JAXBElement {

    public JaxbVarElement() {
        // for serialization use case only
        super(new QName("value"), String.class, new String());
    }

    public JaxbVarElement(QName qName, Class aClass, Object o) {
        super(qName, aClass, o);
    }

    public JaxbVarElement(QName qName, Class aClass, Class aClass1, Object o) {
        super(qName, aClass, aClass1, o);
    }

    @JsonProperty("value")
    @Override
    public Object getValue() {
        return super.getValue();
    }

    @JsonIgnore
    @Override
    public Class getDeclaredType() {
        return super.getDeclaredType();
    }

    @JsonIgnore
    @Override
    public QName getName() {
        return super.getName();
    }

    @JsonIgnore
    @Override
    public Class getScope() {
        return super.getScope();
    }

    @JsonIgnore
    @Override
    public boolean isNil() {
        return super.isNil();
    }

    @JsonIgnore
    @Override
    public boolean isGlobalScope() {
        return super.isGlobalScope();
    }

    @JsonIgnore
    @Override
    public boolean isTypeSubstituted() {
        return super.isTypeSubstituted();
    }
}
