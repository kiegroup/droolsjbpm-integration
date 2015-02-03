package org.kie.server.client;

import org.kie.server.api.marshalling.MarshallingFormat;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;
import java.net.URL;
import java.util.Set;

public interface KieServicesConfiguration {
    public static enum Transport {
        REST, JMS;
    }

    String getServerUrl();

    KieServicesConfiguration setServerUrl(String url);

    String getUserName();

    KieServicesConfiguration setUserName(String userName);

    String getPassword();

    KieServicesConfiguration setPassword(String password);

    MarshallingFormat getMarshallingFormat();

    KieServicesConfiguration setMarshallingFormat(MarshallingFormat format);

    boolean isJms();

    boolean isRest();

    Set<Class<?>> getExtraJaxbClasses();

    boolean addJaxbClasses(Set<Class<?>> extraJaxbClassList);

    KieServicesConfiguration setExtraJaxbClasses(Set<Class<?>> extraJaxbClasses);

    KieServicesConfiguration clearJaxbClasses();

    Transport getTransport();

    long getTimeout();

    KieServicesConfiguration setTimeout(long timeout);

    boolean getUseUssl();

    KieServicesConfiguration setUseSsl(boolean useSsl);

    KieServicesConfiguration setRemoteInitialContext(InitialContext context);

    ConnectionFactory getConnectionFactory();

    KieServicesConfiguration setConnectionFactory(ConnectionFactory connectionFactory);

    Queue getRequestQueue();

    KieServicesConfiguration setRequestQueue(Queue requestQueue);

    Queue getResponseQueue();

    KieServicesConfiguration setResponseQueue(Queue responseQueue);

    void dispose();

    KieServicesConfiguration clone();

}
