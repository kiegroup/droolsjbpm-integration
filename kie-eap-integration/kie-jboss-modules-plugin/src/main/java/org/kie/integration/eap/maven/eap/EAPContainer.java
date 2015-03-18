package org.kie.integration.eap.maven.eap;

import org.apache.maven.artifact.versioning.ComparableVersion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EAPContainer {

    public static enum EAPContainerId {
        EAP, AS;
    }
    private static final Pattern ID_PARSER_PATTERN = Pattern.compile("(.*)-(.*)");
    
    private String eapVersionId;
    private EAPContainerId containerId;
    private ComparableVersion version;
    
    public EAPContainer(String eapVersionId) {
        this.eapVersionId = eapVersionId;
        parseId();
    }
    
    protected void parseId() {
        if (eapVersionId == null || eapVersionId.trim().length() == 0) throw new IllegalArgumentException("EAP Version is empty or null.");

        Matcher m1 = ID_PARSER_PATTERN.matcher(eapVersionId);
        boolean matches = m1.matches();

        if (!matches) throw new IllegalArgumentException("EAP Version '" + eapVersionId + "' does not match the pattern.");  
        
        String _containerId = m1.group(1);
        String _version = m1.group(2);
        
        this.containerId = EAPContainerId.AS.name().equals(_containerId) ? EAPContainerId.AS : EAPContainerId.EAP;
        this.version = new ComparableVersion(_version);
    }
    
    public EAPContainerId getContainerId() {
        return containerId;
    }
    
    public ComparableVersion getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return eapVersionId;
    }
}
