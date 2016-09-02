package org.drools.rhq.plugin;

import org.rhq.plugins.jmx.JMXComponent;
import org.rhq.plugins.jmx.MBeanResourceDiscoveryComponent;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;

import java.util.Set;

public class ExposeKCIDValueFromParentDiscoveryComponent extends MBeanResourceDiscoveryComponent {

    public Set<DiscoveredResourceDetails> performDiscovery(Configuration pluginConfiguration,
                                                           JMXComponent parentResourceComponent, ResourceType resourceType, boolean skipUnknownProps) {
        Set<DiscoveredResourceDetails> services = super.performDiscovery(
                pluginConfiguration, parentResourceComponent, resourceType, skipUnknownProps);

        String kcId = this.discoveryContext.getParentResourceContext()
                .getPluginConfiguration().getSimpleValue("kcontainerId");
        for (DiscoveredResourceDetails service: services) {
            service.getPluginConfiguration().setSimpleValue("kcId", kcId);
        }

        return services;
    }
}
