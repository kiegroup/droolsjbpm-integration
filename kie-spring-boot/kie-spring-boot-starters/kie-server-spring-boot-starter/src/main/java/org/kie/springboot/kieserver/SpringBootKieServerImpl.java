package org.kie.springboot.kieserver;

import java.util.List;

import org.kie.internal.identity.IdentityProvider;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.impl.KieServerImpl;


public class SpringBootKieServerImpl extends KieServerImpl{

    private IdentityProvider identityProvider;
    private List<KieServerExtension> extensions;
    
    
    public SpringBootKieServerImpl(List<KieServerExtension> extensions, IdentityProvider identityProvider) {
        this.extensions = extensions;
        this.identityProvider = identityProvider;
    }
    
    @Override
    protected List<KieServerExtension> sortKnownExtensions() {
        return extensions;
    }

    @Override
    public void init() {        
        super.init();
        getServerRegistry().registerIdentityProvider(identityProvider);
    }


}
