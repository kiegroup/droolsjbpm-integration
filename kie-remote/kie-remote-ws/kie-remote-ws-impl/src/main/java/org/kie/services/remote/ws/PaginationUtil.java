package org.kie.services.remote.ws;

public class PaginationUtil {

    public static int [] getPageInfo(Integer pageNumber, Integer pageSize) { 
       int p = 0;
       if( pageNumber != null ) { 
           p = pageNumber.intValue();
           if( p < 0 ) { 
               p = 0;
           }
       }
       
       int s = 0;
       if( pageSize != null ) { 
           s = pageSize.intValue();
           if( s < 0 ) { 
               s = 0;
           }
       }
      
       int [] pageInfo = { p, s };
       return pageInfo;
    }
}
