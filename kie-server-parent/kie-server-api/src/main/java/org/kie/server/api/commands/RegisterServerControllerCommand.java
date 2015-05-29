package org.kie.server.api.commands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.KieServerConfig;

@XmlRootElement(name="register-server-controller")
@XStreamAlias( "register-server-controller" )
@XmlAccessorType(XmlAccessType.NONE)
public class RegisterServerControllerCommand
        implements KieServerCommand {
    private static final long serialVersionUID = -1803374525440234578L;

    @XStreamAlias("controller-url")
    @XmlElement(name = "controller-url")
    private String controllerUrl;

    private KieServerConfig kieServerConfig;

    public RegisterServerControllerCommand() {
        super();
    }

    public RegisterServerControllerCommand(String controllerUrl, KieServerConfig kieServerConfig) {
        super();
        this.controllerUrl = controllerUrl;
        this.kieServerConfig = kieServerConfig;
    }

    public String getControllerUrl() {
        return controllerUrl;
    }

    public void setControllerUrl(String controllerUrl) {
        this.controllerUrl = controllerUrl;
    }

    public KieServerConfig getKieServerConfig() {
        return kieServerConfig;
    }

    public void setKieServerConfig(KieServerConfig kieServerConfig) {
        this.kieServerConfig = kieServerConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RegisterServerControllerCommand that = (RegisterServerControllerCommand) o;

        if (controllerUrl != null ? !controllerUrl.equals(that.controllerUrl) : that.controllerUrl != null) {
            return false;
        }
        if (kieServerConfig != null ? !kieServerConfig.equals(that.kieServerConfig) : that.kieServerConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = controllerUrl != null ? controllerUrl.hashCode() : 0;
        result = 31 * result + (kieServerConfig != null ? kieServerConfig.hashCode() : 0);
        return result;
    }
}
