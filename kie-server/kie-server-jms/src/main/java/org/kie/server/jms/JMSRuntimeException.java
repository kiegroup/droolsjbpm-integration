package org.kie.server.jms;

/**
 * This exception is primarily used by the JMS code to indicate that an operation
 * that we expect to succeed has failed. In most of the cases, when this exception
 * is thrown, it is due to a failure by the underlying JMS framework ({@link Session},
 * {@link Connection}).
 */
public class JMSRuntimeException
        extends RuntimeException  {

    public JMSRuntimeException() {}

    public JMSRuntimeException(String message) {
        super( message );
    }

    public JMSRuntimeException(String message, Throwable cause) {
        super( message, cause );
    }

    public JMSRuntimeException(Throwable cause) {
        super( cause );
    }

}
