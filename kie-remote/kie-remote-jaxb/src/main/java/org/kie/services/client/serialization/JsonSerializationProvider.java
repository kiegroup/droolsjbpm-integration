/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.services.client.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonSerializationProvider implements SerializationProvider {

    public static final int JMS_SERIALIZATION_TYPE = 1;

    private ObjectMapper mapper = new ObjectMapper();
   
    private Class<?> outputType = null;

    public JsonSerializationProvider() {
    }

    @Override
    public int getSerializationType() { 
        return JMS_SERIALIZATION_TYPE;
    }
    
    @Override
    public String serialize(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonGenerationException jge) {
            throw new SerializationException("Unable to serialize " + object.getClass().getSimpleName() + " instance", jge);
        } catch (JsonMappingException jme) {
            throw new SerializationException("Unable to serialize " + object.getClass().getSimpleName() + " instance", jme);
        } catch (IOException ie) {
            throw new SerializationException("Unable to serialize " + object.getClass().getSimpleName() + " instance", ie);
        }
    }

    @Override
    public Object deserialize(String jsonStr) {
        return deserialize(jsonStr, this.outputType);
    }
    
    public void setDeserializeOutputClass(Class<?> type) {
        this.outputType = type;
    }

    public <T> T deserialize(String jsonStr, Class<T> type) { 
        try {
            return mapper.readValue(jsonStr, type);
        } catch (JsonGenerationException jge) {
            throw new SerializationException("Unable to deserialize String " + type.getSimpleName() + " instance", jge);
        } catch (JsonMappingException jme) {
            throw new SerializationException("Unable to deserialize String " + type.getSimpleName() + " instance", jme);
        } catch (IOException ie) {
            throw new SerializationException("Unable to deserialize String to " + type.getSimpleName() + " instance", ie);
        }
    }

    @Override
    public void dispose() {
        if( this.mapper != null ) { 
            this.mapper = null;
        }
    }

}
