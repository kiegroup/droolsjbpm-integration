package org.kie.server.api.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="kie-server-info")
public class KieServerInfo {
    private String version;
    
    public KieServerInfo() {
        super();
    }

    public KieServerInfo(String version) {
        super();
        this.version = version;
    }

    @XmlElement(name="version")
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "KieServerInfo [version=" + version + "]";
    }
}
