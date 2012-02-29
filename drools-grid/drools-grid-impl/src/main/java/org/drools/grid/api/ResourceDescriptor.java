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

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.Set;

/**
 * Agnostic description of a Resource
 */
public interface ResourceDescriptor extends Serializable{

    /**
     * Returns the author of the Resource.
     * @return the author of the Resource
     */
    public String getAuthor();

    /**
     * Returns the List of categories associated with the Resource.
     * @return the List of categories associated with the Resource
     */
    public Set<String> getCategories();

    /**
     * Returns the creation date of the Resource.
     * @return the creation date of the Resource
     */
    public Date getCreationTime();

    /**
     * Returns the description of the Resource.
     * @return the description of the Resource
     */
    public String getDescription();

    /**
     * Returns the URL of the documentation associated with the Resource.
     * @return the URL of the documentation associated with the Resource
     */
    public URL getDocumentation();

    /**
     * Returns the id of the Resource.
     * @return the id of the Resource
     */
    public String getId();

    /**
     * Returns the date when the Resource was last modified.
     * @return the date when the Resource was last modified
     */
    public Date getLastModificationTime();

    /**
     * Returns the name of the Resource.
     * @return the name of the Resource
     */
    public String getName();

    /**
     * Returns the URL that can be used to get the Resource this descriptor points at.
     * @return the URL that can be used to get the Resource this descriptor points at
     */
    public URL getResourceURL();

    /**
     * Returns the status of the Resource.
     * @return the status of the Resource
     */
    public String getStatus();

    /**
     * Returns the type of the Resource.
     * @return the type of the Resource
     */
    public String getType();

    /**
     * Returns the version of the Resource.
     * @return the version of the Resource
     */
    public String getVersion();

}
