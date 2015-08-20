package org.kie.services.client.serialization;

import org.kie.services.client.serialization.AbstractRemoteSerializationTest.TestType;
import org.yaml.snakeyaml.Yaml;

public class YamlProcessInstancesRemoteSerializationTest extends AbstractProcessInstancesRemoteSerializationTest {

    public TestType getType() { 
        return TestType.YAML;
    }
    
    private Yaml yaml = new Yaml();
    
    public <T> T testRoundTrip(T in) throws Exception {
        String output = yaml.dump(in);
        logger.debug(output);
        return (T) yaml.load(output);
    }

    @Override
    public void addClassesToSerializationProvider(Class<?>... extraClass) {
        // no-op
    }

}
