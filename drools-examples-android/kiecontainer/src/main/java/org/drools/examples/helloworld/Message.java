package org.drools.examples.helloworld;

import java.util.List;

/**
 * @author kedzie
 */
public class Message {
    public static final int HELLO   = 0;
    public static final int GOODBYE = 1;

    private String          message;

    private int             status;

    public Message() {

    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(final int status) {
        this.status = status;
    }

    public static Message doSomething(Message message) {
        return message;
    }

    public boolean isSomething(String msg,
                               List<Object> list) {
        list.add( this );
        return this.message.equals( msg );
    }
}
