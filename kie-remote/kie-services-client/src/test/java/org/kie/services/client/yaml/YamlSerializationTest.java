package org.kie.services.client.yaml;

import org.kie.services.client.SerializationTest;
import org.yaml.snakeyaml.Yaml;

public class YamlSerializationTest extends SerializationTest {

    public TestType getType() { 
        return TestType.YAML;
    }
    
    private Yaml yaml = new Yaml();
    
    public Object testRoundtrip(Object in) throws Exception {
        String output = yaml.dump(in);
        log.debug(output);
        return yaml.load(output);
    }
    
}
