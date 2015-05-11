package org.kie.server.api;

import java.util.Set;

import org.kie.server.api.model.KieContainerResource;

public interface KieController {

    /**
     * Returns list of <code>KieContainerResource</code> retrieved from one of the given controllers.
     * Possible outcomes:
     * <ul>
     *  <li>Non empty list in case sync was performed successfully and there are containers for given server id</li>
     *  <li>Empty list in case sync was performed successfully but there are no containers for given server id</li>
     *  <li>Null in case there is no controllers or not possible to connect to them</li>
     * </ul>
     * @param controllers
     * @param serverId
     * @return
     */
    Set<KieContainerResource> getContainers(Set<String> controllers, String serverId);
}
