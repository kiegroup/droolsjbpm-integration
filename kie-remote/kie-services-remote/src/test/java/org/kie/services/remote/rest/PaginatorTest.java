package org.kie.services.remote.rest;

import static org.kie.services.remote.rest.ResourceBase.getPageNumAndPageSize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.kie.services.remote.util.Paginator;

public class PaginatorTest extends Assert {

    @Test
    public void testPaginate() { 
       
        List<Integer> results = new ArrayList<Integer>();
        for( int i = 0; i < 100; ++i ) { 
            results.add(i);
        }
        
        String oper = "/test/paginate";
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        
        Paginator<Integer> paginator = new Paginator<Integer>();
        
        List<Integer> pagedList = paginator.paginate(pageInfo, results);
        
        assertEquals( results, pagedList );
        
        List<String> pageValues = new ArrayList<String>();
        pageValues.add("2");
        List<String> sizeValues = new ArrayList<String>();
        sizeValues.add("3");
        
        params.put("page", pageValues );
        params.put("pageSize", sizeValues );
        pageInfo = getPageNumAndPageSize(params, oper);
       
        pagedList = paginator.paginate(pageInfo, results);
        assertEquals( new Integer(3),  pagedList.get(0));
        assertEquals( 3, pagedList.size() );
        
        pageValues.clear();
        pageValues.add("4");
        sizeValues.clear();
        sizeValues.add("5");
        params.put("p", pageValues );
        params.put("s", sizeValues );
        pageInfo = getPageNumAndPageSize(params, oper);
       
        pagedList = paginator.paginate(pageInfo, results);
        assertEquals( new Integer(15),  pagedList.get(0));
        assertEquals( 5, pagedList.size() );
    }
}
