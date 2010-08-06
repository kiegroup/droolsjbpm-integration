package org.drools.grid.remote.directory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.drools.SystemEventListener;
import org.drools.grid.DirectoryNodeService;

import org.drools.grid.DirectoryNodeLocalImpl;
import org.drools.grid.internal.GenericIoWriter;
import org.drools.grid.internal.GenericMessageHandler;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.commands.SimpleCommand;
import org.drools.grid.internal.commands.SimpleCommandName;

public class DirectoryServerMessageHandlerImpl implements GenericMessageHandler {

    private final Map<String, GenericIoWriter> clients;
    //We can create a persistent directory service and provide access to that service
    private DirectoryNodeService directory = new DirectoryNodeLocalImpl();
    private final SystemEventListener systemEventListener;

    public DirectoryServerMessageHandlerImpl(SystemEventListener systemEventListener) {

        this.clients = new HashMap<String, GenericIoWriter>();
        this.systemEventListener = systemEventListener;
    }

    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        systemEventListener.exception("Uncaught exception on Server", cause);
    }

    public void messageReceived(GenericIoWriter session, Message msg) throws Exception {
        SimpleCommand cmd = (SimpleCommand) ((Message) msg).getPayload();
        try {
            systemEventListener.debug("Message received on server : " + cmd.getName());
  //          systemEventListener.debug("Arguments : " + Arrays.toString(cmd.getArguments().toArray()));
            switch (cmd.getName()) {
                case RegisterExecutor: {
                    systemEventListener.debug("Command receieved on server was operation of type: RegisterExecutor");
                    String executorId = (String) cmd.getArguments().get(0);
                    String resourceId = (String) cmd.getArguments().get(1);
                    directory.register(executorId, resourceId);
                    

                    SimpleCommand resultsCmnd = new SimpleCommand(cmd.getId(), SimpleCommandName.OperationResponse, null);
                    session.write(new Message( msg.getSessionId(),
                                        msg.getResponseId(),
                                        msg.isAsync(),
                                        resultsCmnd ), null);

                    break;
                }

                 case UnRegisterExecutor: {
                    systemEventListener.debug("Command receieved on server was operation of type: UnRegisterExecutor");
                    String executorId = (String) cmd.getArguments().get(0);

                    directory.unregister(executorId);
                    

                    SimpleCommand resultsCmnd = new SimpleCommand(cmd.getId(), SimpleCommandName.OperationResponse, null);
                    session.write(new Message( msg.getSessionId(),
                                        msg.getResponseId(),
                                        msg.isAsync(),
                                        resultsCmnd ), null);

                    break;
                }

                 case RegisterKBase: {
                    systemEventListener.debug("Command receieved on server was operation of type: RegisterKbase");
                    String kbaseId = (String) cmd.getArguments().get(0);
                    String resourceId = (String) cmd.getArguments().get(1);
                    directory.registerKBase(kbaseId, resourceId);
                    

                    SimpleCommand resultsCmnd = new SimpleCommand(cmd.getId(), SimpleCommandName.OperationResponse, null);
                    session.write(new Message( msg.getSessionId(),
                                        msg.getResponseId(),
                                        msg.isAsync(),
                                        resultsCmnd ), null);

                    break;
                }


                case RequestExecutorsMap: {

                    Map<String, String> directoryMap = directory.getExecutorsMap();
                    List<Object> results = new ArrayList<Object>(1);
                    results.add(directoryMap);
                    SimpleCommand resultsCmnd = new SimpleCommand(cmd.getId(), SimpleCommandName.OperationResponse, results);
                    session.write(new Message( msg.getSessionId(),
                                        msg.getResponseId(),
                                        msg.isAsync(),
                                        resultsCmnd ), null);
                    break;
                }
                case RequestKBasesMap: {

                    Map<String, String> kbasesMap = directory.getKBasesMap();
                    List<Object> results = new ArrayList<Object>(1);
                    results.add(kbasesMap);
                    SimpleCommand resultsCmnd = new SimpleCommand(cmd.getId(), SimpleCommandName.OperationResponse, results);
                    session.write(new Message( msg.getSessionId(),
                                        msg.getResponseId(),
                                        msg.isAsync(),
                                        resultsCmnd ), null);
                    break;
                }
                 case RequestKBaseId: {

                    String kbaseId = (String) cmd.getArguments().get(0);

                    String kbaseConnectorId = directory.getKBasesMap().get(kbaseId);
                    List<Object> results = new ArrayList<Object>(1);
                    results.add(kbaseConnectorId);
                    SimpleCommand resultsCmnd = new SimpleCommand(cmd.getId(), SimpleCommandName.OperationResponse, results);
                    session.write(new Message( msg.getSessionId(),
                                        msg.getResponseId(),
                                        msg.isAsync(),
                                        resultsCmnd ), null);
                    break;
                }
                case RequestLookupSessionId: {

                    String executorId = (String) cmd.getArguments().get(0);
                    
                    String resourceId = directory.getExecutorsMap().get(executorId);
                    List<Object> results = new ArrayList<Object>(1);
                    results.add(resourceId);
                    SimpleCommand resultsCmnd = new SimpleCommand(cmd.getId(), SimpleCommandName.OperationResponse, results);
                    session.write(new Message( msg.getSessionId(),
                                        msg.getResponseId(),
                                        msg.isAsync(),
                                        resultsCmnd ), null);
                    break;
                }
                
                default: {
                    systemEventListener.debug("Unknown command recieved on server");
                }
            }
        } catch (RuntimeException e) {
            systemEventListener.exception(e.getMessage(), e);
            e.printStackTrace(System.err);
        }
    }

    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        systemEventListener.debug("Server IDLE " + session.getIdleCount(status));
    }
}
