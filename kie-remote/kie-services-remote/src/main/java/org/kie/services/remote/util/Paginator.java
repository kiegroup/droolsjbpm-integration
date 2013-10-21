package org.kie.services.remote.util;

import java.util.ArrayList;
import java.util.List;

public class Paginator<T> {

    public List<T> paginate(int [] pageInfo, List<T> results) { 
        List<T> pagedResults = new ArrayList<T>();
        assert pageInfo[0] >= 0;
        if( pageInfo[0] == 0 ) { 
            return results;
        }  else if( pageInfo[0] > 0 ) { 
            // for( i  = start of page; i < start of next page && i < num results; ++i ) 
            for( int i = (pageInfo[0]-1)*pageInfo[1]; i < pageInfo[0]*pageInfo[1] && i < results.size(); ++i ) { 
                pagedResults.add(results.get(i));
            }
        }
        return pagedResults;
    }
}
