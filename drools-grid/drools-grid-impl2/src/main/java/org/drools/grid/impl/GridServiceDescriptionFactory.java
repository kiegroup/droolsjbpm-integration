/*
 * Copyright 2010 salaboy.
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
 * under the License.
 */

package org.drools.grid.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.service.directory.impl.GridServiceDescriptionImpl;

/**
 *
 * @author salaboy
 */
public class GridServiceDescriptionFactory {
    
    public static GridServiceDescription newGridServiceDescritpion(String serviceId){
        GridServiceDescription gsd = new GridServiceDescriptionImpl(serviceId);
        try {
            
            String[] clazzName = serviceId.split(":");
            if(clazzName[0].equals("scheduler")){
                gsd.setImplementedClass(Class.forName("org.drools.grid.timer.impl.SchedulerImpl"));
            }
             if(clazzName[0].equals("schedulerclient")){
                gsd.setImplementedClass(Class.forName("org.drools.grid.timer.impl.SchedulerClient"));
            }
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GridServiceDescriptionFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return gsd;
    }

}
