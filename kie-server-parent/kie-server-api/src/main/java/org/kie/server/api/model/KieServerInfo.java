package org.kie.server.api.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="kie-server-info")
@XStreamAlias( "kie-server-info" )
public class KieServerInfo {

    private String serverId;
    private String version;
    
    public KieServerInfo() {
        super();
    }

    public KieServerInfo(String serverId, String version) {
        super();
        this.serverId = serverId;
        this.version = version;
    }

    @XmlElement(name="version")
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }

    @XmlElement(name="id")
    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    @Override
    public String toString() {
        return "KieServerInfo{" +
                "serverId='" + serverId + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
