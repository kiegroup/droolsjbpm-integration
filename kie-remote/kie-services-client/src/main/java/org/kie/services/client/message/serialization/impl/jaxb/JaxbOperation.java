package org.kie.services.client.message.serialization.impl.jaxb;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.services.client.message.OperationMessage;

@XmlRootElement(name = "operation")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbOperation {

    @XmlAttribute(name = "serviceType")
    @XmlSchemaType(name = "int")
    private int serviceType;

    @XmlAttribute(name = "method")
    @XmlSchemaType(name = "String")
    private String method;

    @XmlAttribute(name = "index")
    @XmlSchemaType(name = "int")
    private Integer index;

    @XmlElement(name = "result")
    private JaxbArgument result;

    @XmlElementRef(name = "args")
    public List<JaxbArgument> args = new ArrayList<JaxbArgument>();

    public JaxbOperation() {
        // Default constructor
    }

    JaxbOperation(OperationMessage origOper) {
        Method origMethod = origOper.getMethod();
        this.method = origMethod.getName();
        this.serviceType = origOper.getServiceType();
        Object[] origArgs = origOper.getArgs();
        if (origOper.isResponse()) {
            this.result = convertToJaxbArgument(origOper.getResult(), origMethod.getReturnType(), 0);
            this.result.setIndex(0);
        } else {
            Class<?> [] types = origMethod.getParameterTypes();
            for (int i = 0; i < origArgs.length; ++i) {
                JaxbArgument arg = convertToJaxbArgument(origArgs[i], types[i], i);
                args.add(arg);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private JaxbArgument convertToJaxbArgument(Object obj, Class<?> type, int i) {
        JaxbArgument arg = null;
        if (obj instanceof Map) {
            arg = new JaxbMap((Map<String, Object>) obj, i);
        } else if (obj == null) {
            arg = new JaxbNullArgument(i);
        } else {
            arg = new JaxbSingleArgument(obj, i);
        }
        return arg;
    }

    public int getServiceType() {
        return serviceType;
    }

    void setServiceType(int serviceType) {
        this.serviceType = serviceType;
    }

    public String getMethod() {
        return method;
    }

    void setMethod(String method) {
        this.method = method;
    }

    public Integer getIndex() {
        return index;
    }

    void setIndex(Integer index) {
        this.index = index;
    }

    public JaxbArgument getResult() {
        return result;
    }

    public List<JaxbArgument> getArgs() {
        return args;
    }

}
