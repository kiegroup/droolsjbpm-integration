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
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        
        List<Integer> pagedList = paginate(pageInfo, results);
        assertEquals( results, pagedList );
        
        List<String> pageValues = new ArrayList<String>();
        pageValues.add("2");
        List<String> sizeValues = new ArrayList<String>();
        sizeValues.add("3");
        
        params.put("page", pageValues );
        params.put("pageSize", sizeValues );
        pageInfo = getPageNumAndPageSize(params, oper);
       
        pagedList = paginate(pageInfo, results);
        assertEquals( new Integer(3),  pagedList.get(0));
        assertEquals( 3, pagedList.size() );
        
        pageValues.clear();
        pageValues.add("4");
        sizeValues.clear();
        sizeValues.add("5");
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
        List<String> sizeValues = new ArrayList<String>();
        sizeValues.add(String.valueOf(pageSize));
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put("pageSize", sizeValues );
        
        List<Integer> pagedResults = paginate(getPageNumAndPageSize(params, oper), results);
        assertEquals( "Paginated results", pageSize, pagedResults.size());
    }
}
