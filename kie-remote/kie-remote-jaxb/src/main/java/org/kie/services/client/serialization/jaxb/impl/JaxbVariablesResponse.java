package org.kie.services.client.serialization.jaxb.impl;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.kie.api.command.Command;
import org.kie.remote.common.jaxb.JaxbRequestStatus;
import org.kie.services.client.serialization.jaxb.impl.map.StringObjectMapXmlAdapter;

@XmlRootElement(name="variables")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbVariablesResponse extends AbstractJaxbCommandResponse<Map<String,String>> {

    @XmlElement
    @XmlJavaTypeAdapter(value=StringObjectMapXmlAdapter.class)
    private Map<String, String> variables = new HashMap<String, String>();
    
    public JaxbVariablesResponse() {
    }
    
    public JaxbVariablesResponse(Command<?> cmd, Map<String, String> variables) {
       this.commandName = cmd.getClass().getSimpleName();
       this.variables = variables;
    }

    public JaxbVariablesResponse(Map<String, String> variables, String requestUrl) { 
        this.url = requestUrl;
        this.status = JaxbRequestStatus.SUCCESS;
    }
    
    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    @Override
    public Map<String, String> getResult() {
        return variables;
    }

    @Override
    public void setResult(Map<String, String> variables) {
        this.variables = variables;
    }
    
}
