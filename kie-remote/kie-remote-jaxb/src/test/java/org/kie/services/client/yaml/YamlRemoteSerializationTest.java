/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.services.client.yaml;

import org.kie.services.client.serialization.AbstractRemoteSerializationTest;
import org.yaml.snakeyaml.Yaml;

public class YamlRemoteSerializationTest extends AbstractRemoteSerializationTest {

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
