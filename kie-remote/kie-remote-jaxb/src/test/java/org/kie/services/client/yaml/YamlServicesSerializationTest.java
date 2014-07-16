package org.kie.services.client.yaml;

import org.kie.services.client.AbstractServicesSerializationTest;
import org.yaml.snakeyaml.Yaml;

public class YamlServicesSerializationTest extends AbstractServicesSerializationTest {

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
