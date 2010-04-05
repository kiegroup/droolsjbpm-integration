package org.drools.grid.task;

import org.drools.Service;
import org.drools.grid.generic.GenericNodeConnector;




public interface HumanTaskFactoryService extends Service {
    public HumanTaskService newHumanTaskService();
    
	 

}
