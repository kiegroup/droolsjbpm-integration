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

import java.io.File;

public class EAPFileResource extends EAPAbstractResource<File> {

    private File resource;

    protected EAPFileResource(String name) {
        super(name);
    }

    public static EAPFileResource create(File artifact) {
        if (artifact == null) return null;

        EAPFileResource result = new EAPFileResource(artifact.getName());
        result.resource = artifact;

        return result;
    }

    @Override
    public File getResource() {
        return resource;
    }

    @Override
    public String getFileName() {
        return resource.getName();
    }

    public void setResource(File resource) {
        this.resource = resource;
    }
}
