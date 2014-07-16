package org.kie.services.client.serialization.jaxb.impl.task;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.jbpm.services.task.impl.model.xml.adapter.StringObjectMapXmlAdapter;
import org.kie.api.command.Command;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;

@XmlRootElement(name = "task-content-response")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbTaskContentResponse implements JaxbCommandResponse<Map<String, Object>> {

    @XmlAttribute
    @XmlSchemaType(name = "int")
    private Integer index;

    @XmlElement(name = "command-name")
    @XmlSchemaType(name = "string")
    private String commandName;

    @XmlElement(name="content-map")
    @XmlJavaTypeAdapter(StringObjectMapXmlAdapter.class)
    private Map<String, Object> contentMap = null;

    public JaxbTaskContentResponse() {
        // Default constructor
    }

    public JaxbTaskContentResponse(int i, Command<?> cmd) {
        this.index = i;
        this.commandName = cmd.getClass().getSimpleName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse#getIndex()
     */
    @Override
    public Integer getIndex() {
        return index;
    }


    @Override
    public void setIndex(Integer index) {
        this.index = index;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse#getCommandName()
     */
    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public void setCommandName(String cmdName) {
        this.commandName = cmdName;
    }

    public JaxbTaskContentResponse(JaxbContent content, int i, Command<?> cmd) {
        this.index = i;
        this.commandName = cmd.getClass().getSimpleName();
        this.contentMap = content.getContentMap();
    }

    public Map<String, Object> getResult() {
        return contentMap;
    }

    @Override
    public void setResult(Map<String, Object> contentMap) {
        this.contentMap = contentMap;
    }

}