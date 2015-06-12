/*
 * Copyright 2015 JBoss Inc
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
package org.jboss.integration.fuse.bxms.Config;

import org.jboss.fuse.eap.config.ConfigSupport;

/**
 * Main class that executes the transformations on the EAP
 * configuration files for using BxMS static module layers.
 */
public class ConfigMain {
    static String PROCESS_NAME = "bxms-integration-config.jar";

    /**
     * Main class for running the BxMS integration pack.
     * @param args The process arguments:
     *             <ol>
     *              <li>
     *                  <code>disable|enable</code> - Enable or disable the configuration on the EAP installation (MANDATORY).     
     *              </li>
     *              <li>
     *                  <code>bpms|brms</code> - Use either BPMS or BRMS module layer. (OPTIONAL - <code>bpms</code> is the default value).    
     *              </li> 
     *             </ol>
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if(args.length<1){
            showHelp();
            System.exit(1);
        } else {
            try {
                final String layer = args.length == 2 ? args[1] : null;
                if (args[0].equals("enable")) {
                    ConfigSupport.applyConfigChange(ConfigSupport.getJBossHome(), true, new BxMSIntegrationConfigEditor(layer));
                } else if (args[0].equals("disable")) {
                    ConfigSupport.applyConfigChange(ConfigSupport.getJBossHome(), false, new BxMSIntegrationConfigEditor(layer));
                } else {
                    showHelp();
                    System.exit(1);
                }
            } catch (ConfigSupport.BadDocument e) {
                System.out.println(e.getMessage());
                System.exit(1);
            } catch (ConfigSupport.CommandException e) {
                System.out.println(e.getMessage());
                System.exit(1);
            }
        }
    }
    
    private static void showHelp() {
        System.out.println(PROCESS_NAME + " disable|enable [bpms|brms]");
    } 

}
