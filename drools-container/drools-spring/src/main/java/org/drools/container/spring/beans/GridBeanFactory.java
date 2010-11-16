/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.container.spring.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.SystemEventListenerFactory;
import org.drools.container.spring.beans.StatefulKnowledgeSessionBeanFactory.JpaConfiguration;
import org.drools.grid.Grid;
import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.conf.impl.GridPeerConfiguration;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.impl.MultiplexSocketServerImpl;
import org.drools.grid.io.AcceptorFactoryService;
import org.drools.grid.io.impl.MultiplexSocketServiceCongifuration;
import org.drools.grid.remote.mina.MinaAcceptorFactoryService;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.CoreServicesLookupConfiguration;
import org.drools.grid.service.directory.impl.WhitePagesLocalConfiguration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author Lucas Amador
 *
 */
public class GridBeanFactory
    implements
    FactoryBean,
    InitializingBean {

    private String           id;
    private GridImpl         grid;

    private Map              coreServices;

    private WhitePages       whitePages;

    private JpaConfiguration jpaConfiguration;
    
    private SocketServiceConfiguration   socketServiceConfiguration ;

    //    private String type;
    //    private GenericConnection connection;
    //
    public Object getObject() throws Exception {
        return this.grid;
    }

    //
    public Class<Grid> getObjectType() {
        return Grid.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        this.grid = new GridImpl( new HashMap() );
        MultiplexSocketServiceCongifuration socketConf = null;
        
        if ( this.coreServices == null ) {
            this.coreServices = new HashMap();
        }

        GridPeerConfiguration conf = new GridPeerConfiguration();
        GridPeerServiceConfiguration coreSeviceLookupConf = new CoreServicesLookupConfiguration( this.coreServices );
        conf.addConfiguration( coreSeviceLookupConf );     
        
        //Configuring the WhitePages 
        if ( this.whitePages != null ) {
            WhitePagesLocalConfiguration wplConf = new WhitePagesLocalConfiguration();
            wplConf.setWhitePages( this.whitePages );
            conf.addConfiguration( wplConf );
        }
      
        conf.configure( this.grid  );
        
        // We do this after the main grid configuration, to make sure all services are instantiated
        if ( this.socketServiceConfiguration != null ) {            
            AcceptorFactoryService acc = null;
            if ( "mina".equals( this.socketServiceConfiguration.getAcceptor() ) ) {
                acc = new MinaAcceptorFactoryService();        
            }

            if ( acc == null ) {
                // Mina is the default for the moment
                acc = new MinaAcceptorFactoryService();
            }
            
            socketConf = new MultiplexSocketServiceCongifuration( new MultiplexSocketServerImpl( this.socketServiceConfiguration.getIp(),
                                                                                                 acc,
                                                                                                 SystemEventListenerFactory.getSystemEventListener(),
                                                                                                 this.grid) );            
            
            for (String[] services : this.socketServiceConfiguration.getServices() ) {
                Object service = ((GridImpl)this.grid).get( services[0].trim() );
                if ( service == null ) {
                    throw new RuntimeException( "Unable to configure socket. Service '" + services[0] + "' could not be found" );
                }
                if ( "auto".equals( services[1].trim() ) ) {
                    
                } else {
                    socketConf.addService( services[0].trim(), service, Integer.parseInt( services[1].trim() ) );
                }
            }
            
            socketConf.configureService( this.grid );
        }           
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public WhitePages getWhitePages() {
        return whitePages;
    }

    public void setWhitePages(WhitePages whitePages) {
        this.whitePages = whitePages;
    }

    public Map getCoreServices() {
        return coreServices;
    }

    public void setCoreServices(Map coreServices) {
        this.coreServices = coreServices;
    }

    public JpaConfiguration getJpaConfiguration() {
        return jpaConfiguration;
    }

    public void setJpaConfiguration(JpaConfiguration jpaConfiguration) {
        this.jpaConfiguration = jpaConfiguration;
    }


    public SocketServiceConfiguration getSocketServiceConfiguration() {
        return socketServiceConfiguration;
    }

    public void setSocketServiceConfiguration(SocketServiceConfiguration socketServiceConfiguration) {
        this.socketServiceConfiguration = socketServiceConfiguration;
    }

    public static class SocketServiceConfiguration {
        private String ip;
        private String acceptor;
        private List<String[]> services;
        
        public String getIp() {
            return ip;
        }
        public void setIp(String ip) {
            this.ip = ip;
        }
        public String getAcceptor() {
            return acceptor;
        }
        public void setAcceptor(String acceptor) {
            this.acceptor = acceptor;
        }        
        
        public List<String[]> getServices() {
            if ( this.services == null ) {
                this.services = new ArrayList<String[]>();
            }
            return services;
        }        
        
        public void setServices(List<String[]> services) {
            this.services = services;
        }
        
        
  
    }

}
