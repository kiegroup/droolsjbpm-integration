package org.drools.grid.remote.mina;

import org.drools.grid.io.Acceptor;
import org.drools.grid.io.AcceptorFactoryService;

public class MinaAcceptorFactoryService
    implements
    AcceptorFactoryService {

    public Acceptor newAcceptor() {
        return new MinaAcceptor();
    }

}
