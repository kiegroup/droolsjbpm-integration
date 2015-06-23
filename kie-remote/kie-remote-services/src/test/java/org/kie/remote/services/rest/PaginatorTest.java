/*
 * Copyright 2015 JBoss Inc
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

package org.kie.remote.services.rest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class PaginatorTest extends ResourceBase {

    @Test
    public void testPaginate() { 
        List<Integer> results = new ArrayList<Integer>();
        for( int i = 0; i < 100; ++i ) { 
            results.add(i);
        }
        
        String oper = "/test/paginate";
        Map<String, String[]> params = new HashMap<String, String[]>();
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        
        List<Integer> pagedList = paginate(pageInfo, results);
        assertEquals( results, pagedList );
        
        String [] pageValues = {"2"};
        String [] sizeValues = {"3"};
        
        params.put("page", pageValues );
        params.put("pageSize", sizeValues );
        pageInfo = getPageNumAndPageSize(params, oper);
       
        pagedList = paginate(pageInfo, results);
        assertEquals( new Integer(3),  pagedList.get(0));
        assertEquals( 3, pagedList.size() );
        
        pageValues[0] = "4";
        sizeValues[0] = "5";
        params.put("p", pageValues );
        params.put("s", sizeValues );
        pageInfo = getPageNumAndPageSize(params, oper);
       
        pagedList = paginate(pageInfo, results);
        assertEquals( new Integer(15),  pagedList.get(0));
        assertEquals( 5, pagedList.size() );
        
        pageInfo[PAGE_NUM] = 0;
        pageInfo[PAGE_SIZE] = 0;
        pagedList = paginate(pageInfo, results);
        assertEquals( pagedList.size(), results.size());
    }
    
    @Test
    public void pageSize10ReturnsAllTasks() { 
        String oper = "/test/paginate";
        List<Integer> results = new ArrayList<Integer>();
        for( int i = 0; i < 100; ++i ) { 
            results.add(i);
        }

        int pageSize = 10;
        String [] sizeValues = {String.valueOf(pageSize)};
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("pageSize", sizeValues );
        
        List<Integer> pagedResults = paginate(getPageNumAndPageSize(params, oper), results);
        assertEquals( "Paginated results", pageSize, pagedResults.size());
    }
}
