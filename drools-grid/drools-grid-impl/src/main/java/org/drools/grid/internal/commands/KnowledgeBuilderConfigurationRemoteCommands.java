/*
 * Copyright 2012 JBoss by Red Hat.
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
package org.drools.grid.internal.commands;

import java.io.Serializable;
import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.grid.remote.KnowledgeBuilderConfigurationRemoteClient;
import org.kie.builder.KnowledgeBuilderConfiguration;

/**
 *
 * @author esteban
 */
public class KnowledgeBuilderConfigurationRemoteCommands implements Serializable {

    /**
     * Extension of PackageBuilderConfiguration that holds grid-related 
     * configuration options.
     */
    private static class GridKnowledgeBuilderConfiguration extends PackageBuilderConfiguration{

        private String messageTimeout;
        private String messageMinWaitTime;
        
        @Override
        public void setProperty(String name, String value) {
            
            if (name.equals(KnowledgeBuilderConfigurationRemoteClient.PROPERTY_MESSAGE_TIMEOUT)){
                this.messageTimeout = value;
            }else if (name.equals(KnowledgeBuilderConfigurationRemoteClient.PROPERTY_MESSAGE_MINIMUM_WAIT_TIME)){
                this.messageMinWaitTime = value;
            }else{
                super.setProperty(name, value);
            }
        }

        @Override
        public String getProperty(String name) {
            if (name.equals(KnowledgeBuilderConfigurationRemoteClient.PROPERTY_MESSAGE_TIMEOUT)){
                return this.messageTimeout;
            }else if (name.equals(KnowledgeBuilderConfigurationRemoteClient.PROPERTY_MESSAGE_MINIMUM_WAIT_TIME)){
                return this.messageMinWaitTime;
            }

            return super.getProperty(name);
        }
        
    }
    
    public static class NewKnowledgeBuilderConfigurationRemoteCommand
            implements GenericCommand<Void> {

        private final String identifier;

        public NewKnowledgeBuilderConfigurationRemoteCommand(String identifier) {
            this.identifier = identifier;
        }

        public Void execute(Context context) {
            context.getContextManager().getContext("__TEMP__").set(identifier, new GridKnowledgeBuilderConfiguration());

            return null;
        }
    }
    
    public static class SetPropertyRemoteCommand
            implements GenericCommand<Void> {

        private final String identifier;
        private final String propertyName;
        private final String propertyValue;

        public SetPropertyRemoteCommand(String identifier, String propertyName, String propertyValue) {
            this.identifier = identifier;
            this.propertyName = propertyName;
            this.propertyValue = propertyValue;
        }

        public Void execute(Context context) {
            KnowledgeBuilderConfiguration kconf = (KnowledgeBuilderConfiguration) context.getContextManager().getContext("__TEMP__").get(identifier);
            kconf.setProperty(propertyName, propertyValue);
            return null;
        }
    }
    
    public static class GetPropertyRemoteCommand
            implements GenericCommand<String> {

        private final String identifier;
        private final String propertyName;

        public GetPropertyRemoteCommand(String identifier, String propertyName) {
            this.identifier = identifier;
            this.propertyName = propertyName;
        }

        public String execute(Context context) {
            KnowledgeBuilderConfiguration kconf = (KnowledgeBuilderConfiguration) context.getContextManager().getContext("__TEMP__").get(identifier);
            return kconf.getProperty(propertyName);
        }
    }
}
