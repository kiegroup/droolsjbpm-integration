/*
 * Copyright 2014 JBoss Inc
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
package org.kie.integration.eap.maven.model.resource;

import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.eclipse.aether.artifact.Artifact;

public class EAPVersionMismatchedArtifactResource extends EAPArtifactResource {

    private String version;
    
    protected EAPVersionMismatchedArtifactResource(String name, String version) {
        super(name);
        this.version = version;
    }

    public static EAPVersionMismatchedArtifactResource create(Artifact artifact, String version) {
        if (artifact == null) return null;

        EAPVersionMismatchedArtifactResource result = new EAPVersionMismatchedArtifactResource(EAPArtifactUtils.getArtifactCoordinates(artifact), version);
        result.resource = artifact;

        return result;
    }

    public String getVersion() {
        return version;
    }
}
