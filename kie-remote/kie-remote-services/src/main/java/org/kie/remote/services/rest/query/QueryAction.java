package org.kie.remote.services.rest.query;


/**
 * This class is a "holder" class to store the following information:
 * <ol>
 * <li>The parameter name, for error messages</li>
 * <li>The action, an integer mapping of the parameter (so that we can use a switch statement)</li>
 * <li>The data, or the value passed with the (query parameter)</li>
 * </ol>
 * Some query parameters, such as certain field values (process instance id, potential owner, etc)
 * can be passed multiple times:
 * 
 * <pre>
 * http://.../../rest/query/runtime/process?processinstanceid=2&processinstanceid=3
 * </pre>
 * 
 * When we process these query parameters on the server side, the JAX-RS logic groups all of the values
 * for one parameter into a list or array.
 * </p>
 * This array of values (for the example above <code>[2,3]</code>) is then assigned to the {@link QueryAction#paramData} field.
 */
class QueryAction {

    public final String paramName;
    public final int action;
    public final String[] paramData;

    public boolean regex = false;
    public boolean min = false;
    public boolean max = false;

    public QueryAction(String param, int action, String[] data) {
        this.paramName = param;
        this.action = action;
        this.paramData = data;
    }
    
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("[" + action + "] " + paramName + ": (");
        if( paramData.length > 0 ) { 
            out.append(paramData[0]);
            for( int i = 1; i < paramData.length; ++i ) { 
                out.append(", ").append(paramData[i]);
            }
        } 
        return out.append(")").toString();
    }
}