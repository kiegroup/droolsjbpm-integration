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

import org.drools.RuleBaseConfiguration;
import org.drools.command.impl.GenericCommand;
import org.drools.grid.remote.KnowledgeBaseConfigurationRemoteClient;
import org.kie.KnowledgeBaseConfiguration;
import org.kie.command.Context;
import org.kie.conf.KieBaseOption;

import java.io.Serializable;

/**
 *
 * @author esteban
 */
public class KnowledgeBaseConfigurationRemoteCommands implements Serializable {

    /**
     * Extension of RuleBaseConfiguration that holds grid-related 
     * configuration options.
     */
    private static class GridKnowledgeBaseConfiguration extends RuleBaseConfiguration{

        private String messageTimeout;
        private String messageMinWaitTime;
        
        @Override
        public void setProperty(String name, String value) {
            
            if (name.equals(KnowledgeBaseConfigurationRemoteClient.PROPERTY_MESSAGE_TIMEOUT)){
                this.messageTimeout = value;
            }else if (name.equals(KnowledgeBaseConfigurationRemoteClient.PROPERTY_MESSAGE_MINIMUM_WAIT_TIME)){
                this.messageMinWaitTime = value;
            }else{
                super.setProperty(name, value);
            }
        }

        @Override
        public String getProperty(String name) {
            if (name.equals(KnowledgeBaseConfigurationRemoteClient.PROPERTY_MESSAGE_TIMEOUT)){
                return this.messageTimeout;
            }else if (name.equals(KnowledgeBaseConfigurationRemoteClient.PROPERTY_MESSAGE_MINIMUM_WAIT_TIME)){
                return this.messageMinWaitTime;
            }

            return super.getProperty(name);
        }

    }
    
    public static class NewKnowledgeBaseConfigurationRemoteCommand
            implements GenericCommand<Void> {

        private final String identifier;

        public NewKnowledgeBaseConfigurationRemoteCommand(String identifier) {
            this.identifier = identifier;
        }

        public Void execute(Context context) {
            context.getContextManager().getContext("__TEMP__").set(identifier, new GridKnowledgeBaseConfiguration());

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
            KnowledgeBaseConfiguration kconf = (KnowledgeBaseConfiguration) context.getContextManager().getContext("__TEMP__").get(identifier);
            kconf.setProperty(propertyName, propertyValue);
            return null;
        }
    }
    
    public static class SetOptionRemoteCommand
            implements GenericCommand<Void> {

        private final String identifier;
        private final KieBaseOption option;

        public SetOptionRemoteCommand(String identifier, KieBaseOption option) {
            this.identifier = identifier;
            this.option = option;
        }

        public Void execute(Context context) {
            KnowledgeBaseConfiguration kconf = (KnowledgeBaseConfiguration) context.getContextManager().getContext("__TEMP__").get(identifier);
            kconf.setOption(option);
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
            KnowledgeBaseConfiguration kconf = (KnowledgeBaseConfiguration) context.getContextManager().getContext("__TEMP__").get(identifier);
            return kconf.getProperty(propertyName);
        }
    }
}
