package org.kie.services.remote.rest.jaxb;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.kie.api.definition.process.Process;
import org.kie.api.io.Resource;
import org.kie.api.task.model.User;

@XmlRootElement(name="processInstance")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbProcess implements Process {

    @XmlElement
    @XmlSchemaType(name="string")
    private String id;

    @XmlElement
    @XmlSchemaType(name="string")
    private String name;

    @XmlElement
    @XmlSchemaType(name="string")
    private String version;

    @XmlElement
    @XmlSchemaType(name="string")
    private String packageName;

    @XmlElement
    @XmlSchemaType(name="string")
    private String type;

    @XmlElement
    @XmlSchemaType(name="int")
    private Integer state;

    @XmlElement
    @XmlSchemaType(name="string")
    private String namespace;

    @XmlElement
    @XmlJavaTypeAdapter(value=KnowledgeTypeXmlAdapter.class, type=User.class)
    private KnowledgeType knowledgeType;
    
    public JaxbProcess() { 
        // Default constructor
    }
    
    public JaxbProcess(Process process) { 
        this.id = process.getId();
        this.name = process.getName();
        this.namespace = process.getNamespace();
        this.packageName = process.getPackageName();
        this.type = process.getType();
        this.version = process.getVersion();
        this.knowledgeType = process.getKnowledgeType();
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getNamespace() {
        return namespace;
    }

    public String getVersion() {
        return version;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public String getType() {
        return type;
    }
    
    public Integer getState() {
        return state;
    }
    
    public KnowledgeType getKnowledgeType() {
        return knowledgeType;
    }

    @Override
    public Map<String, Object> getMetaData() {
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException( methodName + " is not supported on the JAXB " + Process.class.getSimpleName() + " implementation.");
    }

    @Override
    public Resource getResource() {
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException( methodName + " is not supported on the JAXB " + Process.class.getSimpleName() + " implementation.");
    }

    @Override
    public void setResource(Resource res) {
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException( methodName + " is not supported on the JAXB " + Process.class.getSimpleName() + " implementation.");
    } 
}
