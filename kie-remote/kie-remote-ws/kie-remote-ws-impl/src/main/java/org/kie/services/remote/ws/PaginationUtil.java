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
