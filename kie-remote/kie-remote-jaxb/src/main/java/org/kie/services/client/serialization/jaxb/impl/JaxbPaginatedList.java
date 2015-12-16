/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.services.client.serialization.jaxb.impl;

import java.util.List;


public interface JaxbPaginatedList<T> {

    /**
     * @return The page number
     */
    public Integer getPageNumber();

    /**
     * Set the page number
     * @param page The page number
     */
    public void setPageNumber(Integer page);
    
    /**
     * @return The page size
     */
    public Integer getPageSize();

     /**
      * Set the page size
      * @param pageSize The number of items per page
      */
    public void setPageSize(Integer pageSize);
    
    /**
     * Add the list that has been paginated 
     * @param contentList
     */
    public void addContents(List<T> contentList);

}