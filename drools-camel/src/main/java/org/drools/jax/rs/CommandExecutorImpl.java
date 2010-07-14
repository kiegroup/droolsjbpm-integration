package org.drools.jax.rs;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.drools.command.Command;
import org.drools.runtime.CommandExecutor;



public class CommandExecutorImpl implements CommandExecutor {
    
    public CommandExecutorImpl() {
        
    }
    
    public CommandExecutorImpl(boolean restFlag) {
        
    }       

    @POST()
    @Path("/execute")
    @Consumes("text/plain")
    @Produces("text/plain")        
    public <T> T execute(Command<T> command) {
        throw new UnsupportedOperationException( "This should never be called, as it's handled by camel" );
    }
}
