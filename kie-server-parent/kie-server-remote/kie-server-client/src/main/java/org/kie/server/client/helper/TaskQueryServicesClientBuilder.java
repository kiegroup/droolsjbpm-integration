package org.kie.server.client.helper;

import java.util.HashMap;
import java.util.Map;

import org.kie.server.api.KieServerConstants;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.TaskQueryServicesClient;
import org.kie.server.client.impl.TaskQueryServicesClientImpl;

public class TaskQueryServicesClientBuilder implements KieServicesClientBuilder {

	@Override
	public String getImplementedCapability() {
		return KieServerConstants.CAPABILITY_BPM_TASK_QUERIES;
	}

	@Override
	public Map<Class<?>, Object> build(KieServicesConfiguration configuration, ClassLoader classLoader) {
		Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();

        services.put(TaskQueryServicesClient.class, new TaskQueryServicesClientImpl(configuration, classLoader));

        return services;
	}

}
