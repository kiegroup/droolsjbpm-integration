package org.kie.server.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class KieServerEnvironment {
    
    private static final Pattern VERSION_PAT = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)([\\.-].*)?");
    private static Version version;

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

}
