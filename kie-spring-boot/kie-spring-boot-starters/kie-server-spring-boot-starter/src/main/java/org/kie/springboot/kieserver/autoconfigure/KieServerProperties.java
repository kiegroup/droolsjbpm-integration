package org.kie.springboot.kieserver.autoconfigure;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kieserver")
public class KieServerProperties implements InitializingBean {

    private String serverId = "SpringBoot";
    private String serverName = "KieServer-SpringBoot";
    
    private String restContextPath = "/rest/*";
    
    public String getServerId() {
        return serverId;
    }
    
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
    
    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    public String getRestContextPath() {
        return restContextPath;
    }
    
    public void setRestContextPath(String restContextPath) {
        this.restContextPath = restContextPath;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        
    }

}
