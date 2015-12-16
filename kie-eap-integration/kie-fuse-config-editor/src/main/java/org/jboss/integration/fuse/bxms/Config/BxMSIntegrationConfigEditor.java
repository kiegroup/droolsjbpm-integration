/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import de.pdark.decentxml.Document;
import org.jboss.fuse.eap.config.ConfigEditor;
import org.jboss.fuse.eap.config.LayerConfig;

import java.util.Arrays;
import java.util.List;

import static org.jboss.fuse.eap.config.LayerConfig.Type.INSTALLING;

public class BxMSIntegrationConfigEditor implements ConfigEditor
{
   private static final String BPMS_LAYER_NAME = "bpms";
   private String layerName;

   public BxMSIntegrationConfigEditor() {
   }

   public BxMSIntegrationConfigEditor(String layerName) {
      this.layerName = layerName;
   }

   @Override
   public void applyStandaloneConfigChange(boolean enable, Document doc)
   {
      
   }

   @Override
   public void applyDomainConfigChange(boolean enable, Document doc)
   {
      
   }

    public List<LayerConfig> getLayerConfigs() {
       final String l = layerName != null ? layerName : BPMS_LAYER_NAME;
       return Arrays.asList(
            new LayerConfig(l, INSTALLING, -9)
        );
    }
}
