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

package org.kie.server.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class KieServerEnvironment {
    
    private static final Pattern VERSION_PAT = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)([\\.-].*)?");
    private static Version version;
    private static String serverId = System.getProperty(KieServerConstants.KIE_SERVER_ID);
    private static String name = System.getProperty(KieServerConstants.KIE_SERVER_ID);
    private static String contextRoot;

    static {
        String kieServerString = KieServerEnvironment.class.getPackage().getImplementationVersion();
        if (kieServerString == null) {
            InputStream is = null;
            try {
                is = KieServerEnvironment.class.getClassLoader().getResourceAsStream("kie.server.properties");
                Properties properties = new Properties();
                properties.load(is);
                kieServerString = properties.get("kie.server.version").toString();

                is.close();
            } catch ( IOException e ) {
                throw new RuntimeException(e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        Matcher m = VERSION_PAT.matcher(kieServerString);
        if( m.matches() ) {
            try {
                version = new Version( Integer.parseInt(m.group(1)), 
                        Integer.parseInt(m.group(2)), 
                        Integer.parseInt(m.group(3)),
                        m.group(4) );
            } catch (NumberFormatException e) {
                version = new Version(0,0,0,null);
            }
        }

    }

    public static Version getVersion() {
        return version;
    }

    public static String getServerId() {
        return serverId;
    }

    public static void setServerId(String serverIdIn) {
        serverId = serverIdIn;
    }
    public static String getServerName() {
        return name;
    }

    public static void setServerName(String nameIn) {
        name = nameIn;
    }

	public static String getContextRoot() {
		return contextRoot;
	}

	public static void setContextRoot(String contextRootIn) {
		contextRoot = contextRootIn;
	}
    
}
