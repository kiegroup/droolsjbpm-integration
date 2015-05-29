package org.kie.server.services.api;

import java.util.Collection;

/**
 * This is basicaly kie-server CDI. 
 */
public interface KieServerApplicationComponentsService {

    Collection<Object> getAppComponents( String extension, SupportedTransports type, Object... services );

}
