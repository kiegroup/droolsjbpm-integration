package org.kie.server.api.commands;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.kie.server.api.model.*;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "update-scanner")
@XStreamAlias( "update-scanner" )
@XmlAccessorType(XmlAccessType.FIELD)
public class UpdateScannerCommand
        implements KieServerCommand {

    private static final long serialVersionUID = -1803374525440238478L;

    @XStreamAlias("container-id")
    @XmlAttribute(name = "container-id")
    private String containerId;

    @XStreamAlias("scanner")
    @XmlElement
    private KieScannerResource scanner;

    public UpdateScannerCommand() {
        super();
    }

    public UpdateScannerCommand(String containerId, KieScannerResource scanner) {
        this.containerId = containerId;
        this.scanner = scanner;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public KieScannerResource getScanner() {
        return scanner;
    }

    public void setScanner(KieScannerResource scanner) {
        this.scanner = scanner;
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof UpdateScannerCommand) ) return false;

        UpdateScannerCommand that = (UpdateScannerCommand) o;

        if ( containerId != null ? !containerId.equals( that.containerId ) : that.containerId != null ) return false;
        if ( scanner != null ? !scanner.equals( that.scanner ) : that.scanner != null ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = containerId != null ? containerId.hashCode() : 0;
        result = 31 * result + (scanner != null ? scanner.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UpdateScannerCommand{" +
               "containerId='" + containerId + '\'' +
               ", scanner=" + scanner +
               '}';
    }
}
