package org.drools.grid.internal.responsehandlers;

import org.drools.grid.TimeoutException;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.Message;

public class BlockingMessageResponseHandler extends AbstractBaseResponseHandler {

    private long timeout = 60000;
    public long initialWaitTime = 50;
    
    
    
    private volatile Message message;

    public void messageReceived(Conversation conversation,
            Message message) {
        this.message = message;
        setDone(true);
    }

    public Message getMessage() throws RuntimeException, TimeoutException {
        return getMessage(initialWaitTime, timeout);
    }

    public Message getMessage(long initialWaitTime, long timeout) throws RuntimeException, TimeoutException {
        return waitForResponse(initialWaitTime, timeout);
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setInitialWaitTime(long initialWaitTime) {
        this.initialWaitTime = initialWaitTime;
    }
    
    private synchronized Message waitForResponse(long initialWaitTime, long timeout) throws RuntimeException, TimeoutException {

        long waitTime = initialWaitTime;
        
        while (!isDone() && !hasError() && waitTime < timeout){
            try {
                wait(waitTime);
                waitTime *=2;
            } catch ( InterruptedException e ) {
                // swallow and return state of done
            }
        }
        
        if (hasError()){
            throw new RuntimeException(this.getError());
        }
        
        if (isDone() ) {
            return this.message;
        }

        throw new TimeoutException("Timeout waiting for responses ");

    }
}
