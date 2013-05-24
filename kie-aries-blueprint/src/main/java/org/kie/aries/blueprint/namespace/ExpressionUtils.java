/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.aries.blueprint.namespace;

import org.osgi.service.blueprint.container.ComponentDefinitionException;

import java.io.File;

public class ExpressionUtils {
    public static String resolveExpressionInPath(String path){
        //#{ systemProperties['java.io.tmpdir'] }
        String[] pathComponents = path.split("/");
        int i=-1;
        for (String pathComponent : pathComponents){
            i++;
            if ( pathComponent.startsWith("#{")){
                String systemProperty = pathComponent.substring(2).trim();
                if ( systemProperty.endsWith("}")){
                    systemProperty = systemProperty.substring(0, systemProperty.length()-1);
                    systemProperty = systemProperty.trim();
                } else {
                    throw new ComponentDefinitionException("Unable to resolve path ::"+path);
                }
                if ( systemProperty.startsWith("systemProperties") ){
                    systemProperty = systemProperty.substring("systemProperties".length());
                    systemProperty = systemProperty.trim();
                    if ( systemProperty.startsWith("[") && systemProperty.endsWith("]")) {
                        systemProperty = systemProperty.substring(1, systemProperty.length()-1);
                        systemProperty = systemProperty.trim();
                        if ( systemProperty.startsWith("'") && systemProperty.endsWith("'")) {
                            systemProperty = systemProperty.substring(1, systemProperty.length()-1);
                            systemProperty = systemProperty.trim();
                            String value = System.getProperties().getProperty(systemProperty);
                            pathComponents[i] = value;
                        } else {
                            throw new ComponentDefinitionException("Unable to resolve path ::"+path);
                        }
                    } else {
                        throw new ComponentDefinitionException("Unable to resolve path ::"+path);
                    }
                } else {
                    throw new ComponentDefinitionException("Unable to resolve path ::"+path);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String pathComponent : pathComponents){
            sb.append(pathComponent);
            sb.append(File.separator);
        }
        return sb.toString().substring(0, sb.toString().length()-File.separator.length());
    }
}
