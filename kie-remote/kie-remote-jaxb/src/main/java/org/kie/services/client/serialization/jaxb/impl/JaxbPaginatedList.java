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