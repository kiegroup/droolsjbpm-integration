package org.kie.server.api.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "kie-scanner")
public class KieScannerResource {

    private KieScannerStatus status;
    private Long             pollInterval;

    public KieScannerResource() {
    }

    public KieScannerResource(KieScannerStatus status) {
        this(status, null);
    }

    public KieScannerResource(KieScannerStatus status, Long interval) {
        this.status = status;
        this.pollInterval = interval;
    }

    @XmlAttribute(name = "status")
    public KieScannerStatus getStatus() {
        return status;
    }

    public void setStatus(KieScannerStatus status) {
        this.status = status;
    }

    @XmlAttribute(name = "poll-interval")
    public Long getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Long pollInterval) {
        this.pollInterval = pollInterval;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pollInterval == null) ? 0 : pollInterval.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KieScannerResource other = (KieScannerResource) obj;
        if (pollInterval == null) {
            if (other.pollInterval != null)
                return false;
        } else if (!pollInterval.equals(other.pollInterval))
            return false;
        if (status != other.status)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "KieScannerResource [status=" + status + ", pollInterval=" + pollInterval + "]";
    }

}