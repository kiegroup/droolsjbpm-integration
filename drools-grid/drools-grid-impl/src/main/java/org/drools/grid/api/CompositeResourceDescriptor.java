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
package org.drools.grid.api;

import java.util.List;

/**
 * Resource Descriptor containing a List of internal Resource Descriptors
 */
public interface CompositeResourceDescriptor extends ResourceDescriptor{
    
    /**
     * Returns the list of the internal Resources that compose the Resource this
     * class points at.
     * @return the list of the internal Resources that compose the Resource this
     * class points at
     */
    public List<ResourceDescriptor> getInternalResources();
}
