package org.kie.server.api.model.instance;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "work-item-instance-list")
public class WorkItemInstanceList {

    @XmlElement(name="work-item-instance")
    private WorkItemInstance[] workItems;

    public WorkItemInstanceList() {
    }

    public WorkItemInstanceList(WorkItemInstance[] workItems) {
        this.workItems = workItems;
    }

    public WorkItemInstanceList(List<WorkItemInstance> workItems) {
        this.workItems = workItems.toArray(new WorkItemInstance[workItems.size()]);
    }

    public WorkItemInstance[] getWorkItems() {
        return workItems;
    }

    public void setWorkItems(WorkItemInstance[] workItems) {
        this.workItems = workItems;
    }
}
